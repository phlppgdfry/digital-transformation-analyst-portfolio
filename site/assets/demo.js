// Renders cases/01-aankoopproces-bouwgroep/{as-is,to-be}.bpmn with the bpmn.io viewer and links TO-BE tasks
// to the requirements and acceptance tests in the case README.
const $ = id => document.getElementById(id);
const REQ = {
  'REQ-01': ['Must', 'Een werfleider kan mobiel een aanvraag indienen met werf, project, gewenste datum, regels en bijlagen.', 'UAT-01 op mobiel en desktop'],
  'REQ-02': ['Must', 'Het systeem blokkeert indiening wanneer verplichte velden ontbreken of hoeveelheden/prijzen ongeldig zijn.', 'UAT-02 toont begrijpelijke validatiefout'],
  'REQ-03': ['Must', 'De aanvraagroute volgt een door Finance en management goedgekeurde autorisatiematrix.', 'UAT-03 dekt onder, op en boven grensbedragen'],
  'REQ-04': ['Must', 'Alleen goedgekeurde aanvragen worden naar ERP gestuurd; herhaalde berichten maken geen duplicaat.', 'UAT-04 en UAT-05'],
  'REQ-05': ['Should', 'Aanvrager ziet status, verantwoordelijke wachtrij en laatste update.', 'UAT-06'],
  'REQ-06': ['Must', 'Elke beslissing, wijziging en integratie-uitkomst is traceerbaar met actor en tijdstip.', 'Auditlog gecontroleerd in UAT-07'],
  'REQ-07': ['Should', 'Bevoegde medewerkers kunnen een mislukte overdracht veilig opnieuw verwerken.', 'UAT-08 na herstel van testfout'],
  'NFR-02': ['Must', 'De workflow bewaart een correlatie-ID over formulier, flow en ERP-koppeling.', 'Traceerbaarheidstest'],
};
const TRACE = {
  Task_Form: ['REQ-01'], Task_Validate: ['REQ-02'], Gateway_Valid: ['REQ-02'], Task_Correct: ['REQ-02'],
  Task_Review: ['REQ-03'], Gateway_Approved: ['REQ-03', 'REQ-06'], Task_ERP: ['REQ-04', 'NFR-02'], Gateway_ERP: ['REQ-04'],
  Gateway_Retry: ['REQ-07'], Task_Retry: ['REQ-04', 'REQ-07'], Task_Exception: ['REQ-07', 'REQ-06'],
  Task_Notify: ['REQ-05', 'REQ-06'], Task_NotifyReject: ['REQ-05', 'REQ-06'],
};
const PAIN = {
  Task_Email: 'Aanvragen komen via mail en telefoon binnen: geen vaste velden, geen spoor.',
  Task_Retype: 'Aankoop typt alles opnieuw over in ERP of Excel: dubbel werk en tikfouten.',
  Task_Clarify: 'Ontbrekende info wordt achteraf opgevraagd; de aanvraag ligt stil.',
  Task_Approve: 'Goedkeuring loopt via losse kanalen; niemand ziet waar ze vastzit.',
  Timer_NoNews: 'Na twee dagen zonder nieuws begint de opvolging per telefoon.',
  Task_StatusCall: 'De aanvrager belt voor status: onderbrekingen voor aankoop, onzekerheid op de werf.',
  Task_CreateOrder: 'De order wordt pas na het handmatige traject aangemaakt.',
};
const INTRO = {
  'as-is': '<b>Vandaag</b> loopt een werfaanvraag via mail en telefoon, wordt ze overgetypt en zoekt de goedkeuring haar weg via losse kanalen. Klik op een stap voor de frictie die in de workshop gevalideerd moet worden.',
  'to-be': '<b>Voorstel:</b> mobiele intake met validatie, een goedkeuringsmatrix, een idempotente ERP-koppeling met veilige retry en een exception queue, en statusmeldingen. Klik op een taak om te zien welke requirements en UAT-tests ze afdekt.',
};
let viewer, file = 'as-is';
const xml = {};
async function show(f) {
  file = f;
  document.querySelectorAll('#dt-tabs button').forEach(b => b.setAttribute('aria-selected', b.dataset.f === f));
  xml[f] ??= await (await fetch(`assets/${f}.bpmn`)).text();
  await viewer.importXML(xml[f]);
  viewer.get('canvas').zoom('fit-viewport', 'auto');
  $('dt-info').innerHTML = `<p>${INTRO[f]}</p>`;
}
function info(el) {
  const id = el.id, name = el.businessObject?.name || id;
  if (file === 'as-is') {
    $('dt-info').innerHTML = `<h3>${name}</h3><p>${PAIN[id] || 'Geen bijzondere frictie vastgelegd voor deze stap.'}</p><p class="small dim">${INTRO['as-is']}</p>`;
    return;
  }
  const reqs = TRACE[id];
  $('dt-info').innerHTML = `<h3>${name}</h3>` + (reqs ? reqs.map(r => `<div class="dt-req"><div><b class="mono">${r}</b> <span class="pill ${REQ[r][0] === 'Must' ? 'bad' : 'warn'}">${REQ[r][0]}</span></div><p>${REQ[r][1]}</p><div class="small dim">Acceptatie: ${REQ[r][2]}</div></div>`).join('')
    : '<p class="dim">Geen requirement direct aan dit element gekoppeld.</p>');
}
viewer = new BpmnJS({ container: '#dt-canvas' });
viewer.on('element.click', e => { if (e.element.type !== 'bpmn:Process' && e.element.type !== 'label') info(e.element); });
$('dt-tabs').addEventListener('click', e => { const b = e.target.closest('button'); if (b) show(b.dataset.f); });
await show('as-is');
