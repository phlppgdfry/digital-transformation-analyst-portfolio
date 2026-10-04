# Case 02 — AI-assistent voor werfdocumenten

## Doel

Een fictieve bouworganisatie wil dat medewerkers sneller de juiste procedure vinden in goedgekeurde veiligheids-, kwaliteits- en projectdocumenten. De assistent zoekt alleen in toegankelijke bronnen, geeft bronverwijzingen en zegt het wanneer de informatie ontbreekt.

Dit is een oplossingsvoorstel en geen productie-AI-implementatie of advies voor een echte werf.

## Gebruikersvraag

> “Welke goedgekeurde werkinstructie geldt voor activiteit X op project Y?”

## Functionele afspraken

- Antwoord alleen uit documenten waarvoor de gebruiker leesrechten heeft.
- Toon documenttitel, versie, sectie en link bij iedere feitelijke claim.
- Toon geldigheidsdatum en eigenaar van het document.
- Bij ontbrekende, verlopen of tegenstrijdige informatie: geen gok; verwijs naar de bevoegde veiligheids- of projectverantwoordelijke.
- Vraag geen gevoelige persoonsgegevens op en log geen onnodige inhoud.
- Laat de medewerker de bron openen; AI vervangt nooit opleiding, risicoanalyse, toolboxmeeting of formele werkinstructie.

## Conceptarchitectuur

```text
Gebruiker + Entra-identiteit
        ↓ toegangscontrole
Documentindex (goedgekeurde SharePoint/projectbronnen)
        ↓ retrieval met documentversie en ACL-filter
LLM met grounded prompt → antwoord + geciteerde passages
        ↓
Feedback / escalatie / evaluatielog
```

Documentrechten moeten tijdens retrieval worden afgedwongen. Toegangscontrole achteraf op alleen de antwoordpagina is onvoldoende. Beheer bronversies, verwijderde documenten, indexverversing en bewaartermijn expliciet.

## Risico's en controles

| Risico | Controle |
|---|---|
| Verouderde instructie wordt opgehaald | Alleen goedgekeurde bronnen indexeren; versie/eigenaar tonen; verlopen bron blokkeren |
| Antwoord klinkt zeker maar is fout | Antwoord uitsluitend op opgehaalde passages; test abstention; zichtbare broncitaten |
| Gebruiker ziet document buiten zijn rechten | ACL-filter op retrieval; tests met verschillende rollen |
| Prompt injection in geüpload document | Vertrouw documenten als data, nooit als systeeminstructies; adversarial testset |
| AI wordt gezien als veiligheidsgoedkeuring | Heldere gebruikstekst, menselijke escalatie en geen autonome beslissingen |
| Persoons- of projectdata lekt naar logging | Minimaliseer logs, retentie en toegang; security/privacy review |
| Lage antwoordkwaliteit | Evaluatieset met bekende antwoorden, geen-antwoordcases en broncontrole |

## MVP en acceptatie

1. Selecteer één documentdomein en een beperkte pilotgroep.
2. Inventariseer bronhouder, versiebeheer, classificatie en toegangsrollen.
3. Maak een gouden evaluatieset met antwoorden, verwachte passages en gevallen waarop de assistent moet weigeren.
4. Meet bronjuistheid, antwoordnauwkeurigheid, correcte abstention, latency en feedback.
5. Laat proceseigenaar, IT-security, privacy en veiligheid akkoord geven vóór pilot.

**Voorlopige pilotcriteria:** minstens 90% van testvragen citeert de juiste geldige bron; 100% van vooraf gedefinieerde veiligheidskritische/geen-bron-vragen escaleert; nul ongeautoriseerde bronresultaten. Drempels moeten door de organisatie worden vastgesteld.

## KPI's

- Tijd tot gevonden goedgekeurde instructie (baseline versus pilot)
- Percentage antwoorden met juiste bron en versie
- Correcte weigering bij ontbrekende/tegenstrijdige bron
- Ongeautoriseerde retrieval-incidenten (doel: nul)
- Gebruikersfeedback en percentage geëscaleerde vragen

## Besluiten vóór bouw

- Welke documenten zijn gezaghebbend en wie keurt ze goed?
- Hoe wordt SharePoint- of projecttoegang doorgegeven aan retrieval?
- Welke model- en dataresidentie-eisen gelden?
- Welke vraagtypen zijn expliciet uitgesloten?
- Wie behandelt feedback, bronconflicten en incidenten?

