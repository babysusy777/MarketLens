Tutorial per il deploy e utilizzare l’app correttamente:

1. Connettersi alla VPN
2. Aprire tre terminali diversi, e su ciascuno scrivere: ssh root@<IP_VM>.
	Chiederà in tutti e tre i casi la password: root.
3. Su tutte e tre le macchine, avviare mongod con questo comando: 
	
	mongod --replSet lsmdb --dbpath ~/data --port 27017 --bind_ip localhost,10.1.1.67 --oplogSize 200

4. Aprire due nuovi terminali: sul primo, connettersi di nuovo con ssh root@10.1.1.66 sulla 	macchina su cui c’è Neo4j, e attivare neo4j con:
	
	systemctl start neo4j
	
	sudo systemctl status neo4j --no-pager (check se è attivo)
	
NOTA: se vuoi far partire la app con il graph db completamente vuoto, esegui in ordine:
	
	cypher-shell -u neo4j -p marketlens1
	
	MATCH (n) DETACH DELETE n;
	

	Sul secondo terminale, connettersi alla VM Primary con ssh root@10.1.1.65, ed eseguire:
	
	nc -vz 10.1.1.66 7687	(check se si è connessi a neo4j)
	
	java -jar /opt/marketlens/app.jar --spring.config.additional-location=file:/opt/marketlens/config/

La app è partita. Per accedere allo swagger: http://10.1.1.65:8080/swagger-ui/index.html

NOTA: se vuoi far partire la app con il document db completamente vuoto, esegui in ordine:
	
	mongosh
	
	use marketlens
	
	db.dropDatabase()

	show dbs (check)
	
	exit

Per chiudere l’app, eseguire ^C. Dopodiché:

	- Sul terminale della VM 10.1.1.66 (quello su cui non sta runnando mongod) 	eseguire:
	
	 	sudo systemctl stop neo4j
	
	- Su tutti e tre i terminali su cui sta runnando mongod, eseguire ^C (anche più 	volte se necessario) e aspettare che i processi terminino.

	- Chiudere il terminale di VM 10.1.1.66. e VM 10.1.1.65.


IN CASO DI ERRORE DI COMPILAZIONE:

Qualora l’app fallisca a partire per errori di compilazione, e sia necessario riscrivere parte di codice, ecco cosa fare:

	- Ripetere la procedura di sopra di chiusura, senza però terminare i processi di 	mongod e senza chiudere i terminali con le VM 66 5 65.
	
	- Riscrivere il codice su Intellij
	
	- Aprire un nuovo terminale, accedere via terminale alla cartella di MarketLens, e 	arrivare dentro alla cartella target
	 
	(e.g.) user/Downloads/MarketLens/target
	 
	ed eseguire: 
	
	scp MarketLens-0.0.1-SNAPSHOT.jar root@10.1.1.65:/opt/marketlens/app.jar

	- riavviare l’applicazione, avendo cura di pulire i database prima di lanciare il 	comando.
