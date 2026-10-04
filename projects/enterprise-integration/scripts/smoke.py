#!/usr/bin/env python3
import json, os, time, uuid, urllib.request, urllib.error, subprocess
BASE=os.environ.get('LAB_URL','http://localhost:18081/integration-lab/api')
def call(path, method='GET', body=None):
    req=urllib.request.Request(BASE+path,data=body.encode() if body is not None else None,method=method,headers={'Content-Type':'application/xml'})
    try:
        with urllib.request.urlopen(req,timeout=15) as r: return r.status,json.load(r)
    except urllib.error.HTTPError as e: return e.code,json.load(e)
for _ in range(90):
    try:
        if call('/health')[0]==200: break
    except (OSError,ValueError): pass
    time.sleep(2)
else: raise SystemExit('API not ready: inspect docker compose logs')
def xml(mid,partner='ACME',quantity=3):
    return f'<Order><MessageId>{mid}</MessageId><Partner>{partner}</Partner><Sku>BOX</Sku><Quantity>{quantity}</Quantity></Order>'
id='smoke-'+uuid.uuid4().hex
before=next(r for r in call('/reports/partners')[1] if r['code']=='ACME')
assert call('/messages','POST',xml(id))[0]==201
assert call('/messages','POST',xml(id))[0]==409
assert call('/messages/'+id+'/reprocess','POST')[0]==200
row=call('/messages/'+id)[1]
assert row['status']=='PROCESSED' and row['attempts']==1
assert call('/messages','POST',xml(id+'bad',quantity=-1))[0]==400
assert call('/messages','POST',"<!DOCTYPE Order [<!ENTITY x SYSTEM 'file:///etc/passwd'>]>"+xml(id+'xxe'))[0]==400
assert call('/messages/not-found')[0]==404
partner='T'+uuid.uuid4().hex[:12].upper()
failed=id+'f'
status,row=call('/messages','POST',xml(failed,partner))
assert status==422 and row['error_code']=='UNKNOWN_PARTNER'
# Resolve the incident through SQL and replay the stored original payload.
sql=f"WHENEVER SQLERROR EXIT SQL.SQLCODE\nINSERT INTO partners VALUES ('{partner}','Smoke partner');\nCOMMIT;\nEXIT;\n"
subprocess.run(['bash','scripts/sql.sh'],input=sql,text=True,check=True,capture_output=True)
assert call('/messages/'+failed+'/reprocess','POST')[0]==200
assert call('/messages/'+failed)[1]['attempts']==2
# Concurrent retries must not create duplicate orders.
from concurrent.futures import ThreadPoolExecutor
with ThreadPoolExecutor(max_workers=4) as pool:
    assert all(code==200 for code,_ in pool.map(lambda _:call('/messages/'+failed+'/reprocess','POST'),range(4)))
after=next(r for r in call('/reports/partners')[1] if r['code']=='ACME')
assert after['order_count']==before['order_count']+1
assert after['total_quantity']==before['total_quantity']+3
recovered=next(r for r in call('/reports/partners')[1] if r['code']==partner)
assert recovered['order_count']==1 and recovered['total_quantity']==3
print('OK: Oracle persistence, mapping, duplicates, validation, XXE, failed message, SQL repair, concurrent replay and report')
