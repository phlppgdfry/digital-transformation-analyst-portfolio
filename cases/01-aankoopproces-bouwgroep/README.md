# Case 01 — Digitalisering aankoopproces voor een bouwgroep

## Samenvatting

Een fictieve middelgrote bouwgroep ontvangt werfaanvragen via e-mail en telefoon. Aankoop neemt gegevens over in ERP, goedkeuringen lopen deels via losse berichten en aanvragers bellen om de status te kennen. De voorgestelde pilot brengt aanvraag, goedkeuring, ERP-registratie en statuscommunicatie samen in één traceerbare workflow.

**Doel:** een analysepakket tonen dat een BA met proceseigenaars, IT en implementatiepartner kan valideren. Dit is een conceptcase, geen beschrijving van Verhelst Group.

## Probleem en gewenste uitkomst

| Huidige frictie (te valideren) | Gewenste uitkomst |
|---|---|
| Gegevens worden opnieuw overgetypt | Eenmalige invoer met validatie |
| Goedkeuring is verspreid over kanalen | Beslisregels en auditspoor op één plek |
| Aanvrager kent de status niet | Zelfbediening met statusmeldingen |
| Spoed en onvolledige aanvragen komen door elkaar | Prioriteit en volledigheidscontrole vóór verwerking |
| Rapportering wordt handmatig samengesteld | Doorlooptijd en uitzonderingen meetbaar |

## Scope

**In scope:** aankoopaanvraag vanaf mobiel, verplichte project/werf- en kostenplaatsgegevens, goedkeuringsmatrix, ERP-aanmaak na goedkeuring, statuszichtbaarheid, exception queue, auditlog, UAT en training.

**Buiten scope voor de eerste pilot:** volledige leveranciersportaal, automatische contractonderhandeling, autonome AI-goedkeuring, vervanging van ERP, factuurautomatisering en wijzigingen aan financiële boekingsregels.

## Stakeholders en betrokkenheid

| Stakeholder | Belang / bijdrage | Betrekken via |
|---|---|---|
| Werfleider | Snel aanvragen, mobiel gebruik, status | Observatie en prototype-test |
| Aankoop | Volledige aanvragen, leverancierskeuze, uitzonderingen | Procesworkshop en beslisregels |
| Finance | Budget, kostenplaats, audit en functiescheiding | Regelvalidatie en UAT |
| Magazijn | Beschikbaarheid en ontvangst | Interface- en uitzonderingsscenario's |
| Management | Autorisatielimieten en KPI's | Besluitmomenten en benefits review |
| IT / ERP-partner | Integratie, identiteit, beheerbaarheid | Architectuurreview en technische refinement |
| Leverancier | Correcte orderbevestiging | Alleen in latere scope, na interne pilot |

## Procesmodellen

- [AS-IS BPMN 2.0](as-is.bpmn): aanvraag via mail/telefoon, handmatige overname, onduidelijke status.
- [TO-BE BPMN 2.0](to-be.bpmn): digitale intake, regels, goedkeuring, ERP en statuslus.

De BPMN-bestanden bevatten nu ook diagramlayout (BPMN DI, gegenereerd met bpmn-auto-layout), zodat ze direct openen in Camunda Modeler of op bpmn.io. Bekijk ze ook [interactief op de case-site](https://phlppgdfry.github.io/digital-transformation-analyst-portfolio/#demo). Valideer lanes, lokale werkinstructies en uitzonderingen met gebruikers voordat ze als definitief proces gelden.

**Modelcorrecties (17 november 2026):**

- TO-BE: na "Werk status en auditspoor bij" vertrokken twee voorwaardeloze stromen naar *Orderreferentie gedeeld* én *Afwijzing met reden*. In BPMN is dat een parallelle splitsing: elke aanvraag zou in beide eindes terechtkomen. Goedgekeurde en afgewezen aanvragen hebben nu elk een eigen meldtaak.
- TO-BE: een ERP-fout leidde naar een retry-lus zonder uitweg. Een beslissing "Nieuwe poging toegestaan?" stuurt na de limiet naar de exception queue (in scope), waar aankoop de oorzaak oplost.
- AS-IS: "Aanvrager belt voor status" had geen inkomende stroom en kon dus nooit gebeuren. Een niet-onderbrekende timer op de goedkeuringsstap ("Na 2 dagen geen nieuws") start die taak nu.

## Kernrequirements

| ID | Requirement | Prioriteit | Acceptatiebewijs |
|---|---|---|---|
| REQ-01 | Een werfleider kan mobiel een aanvraag indienen met werf, project, gewenste datum, regels en bijlagen. | Must | UAT-01 op mobiel en desktop |
| REQ-02 | Het systeem blokkeert indiening wanneer verplichte velden ontbreken of hoeveelheden/prijzen ongeldig zijn. | Must | UAT-02 toont begrijpelijke validatiefout |
| REQ-03 | De aanvraagroute volgt een door Finance en management goedgekeurde autorisatiematrix. | Must | UAT-03 dekt onder, op en boven grensbedragen |
| REQ-04 | Alleen goedgekeurde aanvragen worden naar ERP gestuurd; herhaalde berichten maken geen duplicaat. | Must | UAT-04 en UAT-05 |
| REQ-05 | Aanvrager ziet status, verantwoordelijke wachtrij en laatste update. | Should | UAT-06 |
| REQ-06 | Elke beslissing, wijziging en integratie-uitkomst is traceerbaar met actor en tijdstip. | Must | Auditlog gecontroleerd in UAT-07 |
| REQ-07 | Bevoegde medewerkers kunnen een mislukte overdracht veilig opnieuw verwerken. | Should | UAT-08 na herstel van testfout |
| NFR-01 | Rollen beperken toegang tot aanvragen volgens organisatie en werf. | Must | Negatieve autorisatietest |
| NFR-02 | De workflow bewaart een correlatie-ID over formulier, flow en ERP-koppeling. | Must | Traceerbaarheidstest |

### User stories

- Als werfleider wil ik een aanvraag op mijn telefoon invullen zodat ik gegevens niet later opnieuw moet overtypen.
- Als aankoper wil ik incomplete of urgente aanvragen herkennen zodat ik ze gericht kan behandelen.
- Als budgethouder wil ik alleen aanvragen binnen mijn bevoegdheid goedkeuren zodat financiële controle behouden blijft.
- Als aanvrager wil ik de actuele status zien zodat ik niet telefonisch hoef op te volgen.
- Als supportmedewerker wil ik integratiefouten kunnen herverwerken zonder dubbele ERP-order te maken.

## Oplossingsrichting

```text
Mobiele aanvraag → validatie → autorisatieflow → ERP-adapter → ERP-aankooporder
        ↑                   ↓                   ↓
 statusportaal ← audit/status store ← exceptions & herstelwachtrij
```

Power Apps of een gelijkwaardig formulier kan de intake verzorgen. Power Automate of een workflowservice behandelt toewijzing en meldingen. ERP blijft eigenaar van leveranciers, artikeldata, boekingsregels en het definitieve ordernummer. Een adapter begrenst de koppeling; een correlatie-ID en idempotency key voorkomen dubbel verwerken. De keuze tussen standaardconnector, API of partneroplossing vraagt een fit-gap met de gekozen ERP-versie en licenties.

## Goedkeuringsregels — voorstel voor workshop

1. Aanvraag wordt gevalideerd op werf, project, budgethouder, artikel/dienst en gewenste leverdatum.
2. De werfverantwoordelijke bevestigt noodzaak en ontvangstlocatie.
3. Budgethouder of aankoop keurt goed op basis van bedrag, categorie, budget en functiescheiding.
4. Boven een nog door management vast te leggen drempel volgt een extra goedkeuring.
5. Na goedkeuring maakt ERP de aankooporder aan; bij fout gaat de aanvraag naar een zichtbare uitzonderingswachtrij.
6. Aanvrager ontvangt status en, na ERP-aanmaak, het orderreferentienummer.

Bedragen en bevoegdheden zijn bewust niet ingevuld: ze moeten uit de echte delegatiematrix komen.

## UAT-scenario's

| ID | Scenario | Verwacht resultaat |
|---|---|---|
| UAT-01 | Geldige aanvraag mobiel indienen | Aanvraag krijgt ID en status `In review` |
| UAT-02 | Verplichte werf ontbreekt | Geen indiening; veldgerichte foutmelding |
| UAT-03 | Bedrag net onder/boven beslisgrens | Juiste budgethouder(s) toegewezen volgens goedgekeurde matrix |
| UAT-04 | Afwijzing door budgethouder | Geen ERP-order; reden en actor gelogd; aanvrager geïnformeerd |
| UAT-05 | Zelfde goedgekeurde aanvraag tweemaal aangeboden | Eén ERP-order; tweede poging veilig als duplicaat afgehandeld |
| UAT-06 | Aanvrager opent statuspagina | Alleen eigen/geautoriseerde aanvragen zichtbaar |
| UAT-07 | Gebruiker wijzigt aanvraag na goedkeuring | Oude goedkeuring vervalt waar de wijzigingsregel dat vereist; wijziging gelogd |
| UAT-08 | ERP is tijdelijk onbereikbaar | Aanvraag blijft traceerbaar; veilige retry; geen dubbele order |
| UAT-09 | Ontvanger meldt gedeeltelijke levering | Ontvangst/tekort zichtbaar volgens afgesproken ERP-proces |

## KPI's en businesscase

Start met een nulmeting van minstens twee representatieve weken. De onderstaande targets zijn voorstelwaarden, geen gerealiseerde resultaten.

| KPI | Definitie | Voorlopig pilotdoel | Eigenaar |
|---|---|---:|---|
| Doorlooptijd aanvraag → besluit | Mediaan en P90 van werktijd | -25% t.o.v. nulmeting | Aankoop |
| First-time-right | Volledige aanvragen bij eerste indiening / totaal | +20 procentpunt | Werf + aankoop |
| Handmatige herinvoer | Aantal overtypmomenten per aanvraag | -50% | Proceseigenaar |
| Statusvragen | Opvolgcontacten per 100 aanvragen | -30% | Aankoop |
| Integratiesucces | Automatisch verwerkte goedgekeurde aanvragen | ≥98% na stabilisatie | IT |
| Adoptie | Actieve pilotgebruikers / beoogde gebruikers | ≥80% | Change lead |

Businesscaseformule: `jaarlijkse baten = vermeden verwerkingstijd + vermeden herstelwerk + aantoonbaar vermeden spoedkosten`. Vergelijk met licentie-, bouw-, beheer-, training- en veranderkosten. Finance valideert de aannames; schaal pas op na pilotmeting.

## Risico's en beheersing

| Risico | Beheersing |
|---|---|
| Onjuiste autorisatiematrix | Finance/management tekenen regels af; tests op grensgevallen |
| Slechte masterdata (werf, leverancier, artikel) | Datakwaliteitscheck en eigenaar vóór automatisering |
| Dubbele order bij timeout/retry | Idempotency key, correlatie en reconciliatie |
| Mobiele adoptie blijft laag | Test op werf, korte training en feedbackkanaal |
| Te brede toegang tot kosten/bijlagen | Least privilege, roltests, bewaartermijn en logging |
| Scope groeit richting volledige P2P | Pilotgrens, change control en aparte roadmap |

## Pilot en rollout

1. **Discover:** interviews, observatie, nulmeting, systeem- en datacheck.
2. **Design:** gevalideerde BPMN, autorisatiematrix, fit-gap en klikbaar prototype.
3. **Pilot:** één werf, beperkte categorieën, key users, dagelijkse exception review.
4. **Evaluate:** KPI-vergelijking, UAT-defecten, gebruikersfeedback en supportbelasting.
5. **Scale:** alleen na besluitpoort, beheerafspraken, training en gefaseerde werfuitrol.

## Open vragen voor echte discovery

- Welke ERP-versie en aankoopfunctionaliteit zijn aanwezig?
- Hoe verlopen vandaag budgetreservering, ontvangst en factuurmatching?
- Welke gegevens mogen werfleiders zelf kiezen en welke komen uit masterdata?
- Hoe ziet de formele delegatie- en vervangingsregeling eruit?
- Welke locaties/connectiviteit en mobiele toestellen moeten worden ondersteund?
- Welke privacy-, bewaartermijn-, licentie- en securityregels gelden?
- Wie beheert exceptions buiten kantooruren en wat is de gewenste responstijd?

