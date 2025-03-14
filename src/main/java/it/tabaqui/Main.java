package it.tabaqui;

import java.io.InputStream;
import java.time.Duration;
import java.util.Arrays;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.openmbean.CompositeData;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

public class Main {
	
	// Static fields
	
	public static final Pattern PROPERTY_PATTERN = Pattern.compile("\\s*([a-zA-Z0-9._-]+)\\s*=\\s*(.*)\\s*");
	
	// Static methods
	
	public static void main(String[] args) throws Exception {
		// Lettura properties da file
		Properties properties = new Properties();
		try (InputStream is = Main.class.getClassLoader().getResourceAsStream("configuration.properties")) {
			properties.load(is);
		}
		// Sovrascrittura properties lette con quelle fornite come argomenti (in formato compatibile chiave=valore)
		Arrays.stream(args)
				.map(PROPERTY_PATTERN::matcher)
				.filter(Matcher::matches)
				.forEach((v) -> properties.setProperty(v.group(1), v.group(2)));
		// Lettura/validazione parametri
		String host = properties.getProperty("tabaqui.host", "");
		if (host.isBlank()) {
			throw new IllegalArgumentException("tabaqui.host: valore non valido");
		}
		int port;
		try {
			port = Integer.parseInt(properties.getProperty("tabaqui.port"));
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException("tabaqui.port: valore non valido");
		}
		long rate;
		try {
			rate = Long.parseLong(properties.getProperty("tabaqui.refresh-rate"));
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException("tabaqui.refresh-rate: valore non valido");
		}
		if (rate <= 0) {
			throw new IllegalArgumentException("tabaqui.refresh-rate: valore non valido");
		}
		// Connessione a JVM remota
		try (JMXConnector connector = JMXConnectorFactory
				.connect(new JMXServiceURL(String.format("service:jmx:rmi:///jndi/rmi://%s:%d/jmxrmi", host, port)))) {
			// Recupero connessione e costruzione ObjectName per MBean da monitorare
			MBeanServerConnection connection = connector.getMBeanServerConnection();
			ObjectName name = new ObjectName("java.lang:type=Memory");
			// Ciclo di lettura dell'attributo HeapMemoryUsage
			while (true) {
				CompositeData data = CompositeData.class.cast(connection.getAttribute(name, "HeapMemoryUsage"));
				long init = Long.class.cast(data.get("init"));
				long used = Long.class.cast(data.get("used"));
				long committed = Long.class.cast(data.get("committed"));
				long max = Long.class.cast(data.get("max"));
				System.out.println(String.format("Init: %d (%dMB); used: %d (%dMB); committed: %d (%dMB); max: %d (%dMB)", init, init / (1024 * 1024), used, used / (1024 * 1024), committed, committed / (1024 * 1024), max, max / (1024 * 1024)));
				Thread.sleep(Duration.ofMillis(rate));
			}
		}
	}
}
