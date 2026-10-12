# R. ITA TV 2.1.29 LAB

- Aggiunto DNS diretto interno su UDP/53 per Cloudflare, Google, Quad9 e AdGuard.
- Cloudflare usa direttamente `1.1.1.1` e `1.0.0.1`, replicando dentro R. ITA la configurazione DNS che funziona sul tablet.
- Ordine di risoluzione: DNS diretto selezionato, DNS-over-HTTPS, DNS Android come ultimo ripiego.
- La correzione vale per VOD, Streaming Community, CB01, resolver, immagini, sorgenti e nuovo LAB multi-provider.
- Nessuna modifica al resolver o al player del percorso Streaming Community LAB 2.1.8.
