# cli
## Descrizione
cli è un'applicazione a riga di comando con lo scopo di monitorare via JMX la memoria heap utilizzata da una JVM remota. I valori letti verranno stampati su console con una frequenza specificata.
## Parametri
I parametri di configurazione verranno letti dalla risorsa **configuration.properties** e potranno essere sovrascritti tramite riga di comando con la sintassi **chiave=valore**. I parametri gestiti sono i seguenti:
### tabaqui.host
Nome host della JVM remota alla quale collegarsi. Default: **localhost**
### tabaqui.port
Porta della JVM remota alla quale collegarsi. Default: **9999**
### tabaqui.refresh-rate
Intervallo in millisecondi tra una lettura e la successiva. Default: **1000**
## Requisiti
* Java 21
## Esecuzione
L'applicazione potrà essere eseguita tramite riga di comando con la seguente sintassi:
```
java -jar ./cli.jar
```
E' possibile sovrascrivere i parametri di default passando coppie chiave-valore direttamente tramite riga di comando:
```
java -jar ./cli.jar tabaqui.port=9998
```
