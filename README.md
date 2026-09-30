dokprod-infotrygdbrev
=====================

`dokprod-infotrygdbrev` er en app som kjører batchen `bdok100` i nais.
`bdok100` BehandleInfotrygdbrev leser linjeprintfiler og tilhørende metadata,
samt kontaktinformasjon fra Infotrygd Dynamisk Kontaktinformasjon. Batchen
prosesserer dataene og lager en dokumentbestillingsXML for hvert dokument som
legges på kø til `qdok001` for produksjon av dokumentene.

Batchen ble tidligere kjørt i `dokprod-batch`, som skal saneres.


## Henvendelser

Lag en issue i repository.

---

## For Nav-ansatte

For mer info om batchen kan du sjekke [dokumentasjonen for `bdok100` i confluence](https://confluence.adeo.no/spaces/BOA/pages/193992372/dokprodbatch+-+BDOK100)

### Henvendelser
Spørsmål om koden eller prosjektet kan rettes til [Slack-kanalen for \#Team Dokumentløsninger](https://nav-it.slack.com/archives/C6W9E5GPJ).

## Lisens

[MIT](LICENSE.md)
