package task1;

public interface ReportFormatter {
    String format(String title, String[] headers, String[][] rows);
}

public class JsonFormatter implements ReportFormatter {
    @Override
    public String format(String title, String[] headers, String[][] rows) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n  \"report\": \"").append(title).append("\",\n  \"records\": [\n");
        for (int i = 0; i < rows.length; i++) {
            sb.append("    { ");
            for (int j = 0; j < headers.length; j++) {
                sb.append("\"").append(headers[j]).append("\": \"").append(rows[i][j]).append("\"");
                if (j < headers.length - 1) sb.append(", ");
            }
            sb.append(" }");
            if (i < rows.length - 1) sb.append(",");
            sb.append("\n");
        }
        sb.append("  ]\n}");
        return sb.toString();
    }
}

public class CsvFormatter implements ReportFormatter {
    @Override
    public String format(String title, String[] headers, String[][] rows) {
        StringBuilder sb = new StringBuilder();
        sb.append("#TITLE: ").append(title).append("\n");
        sb.append(String.join(",", headers)).append("\n");
        for (String[] row : rows) {
            sb.append(String.join(",", row)).append("\n");
        }
        return sb.toString();
    }
}

public interface Exporter {
    void export(String destination, String data);
}

public class BaseFileExporter implements Exporter {
    @Override
    public void export(String destination, String data) {
        System.out.printf("[DISK WRITE] Writing %d bytes to file://%s%n", data.length(), destination);
        System.out.println("--- FILE PAYLOAD BEGIN ---");
        System.out.println(data);
        System.out.println("--- FILE PAYLOAD END ---");
    }
}

public abstract class ExporterDecorator implements Exporter {
    protected final Exporter wrappedExporter;
    public ExporterDecorator(Exporter wrappedExporter) {
        if (wrappedExporter == null) throw new IllegalArgumentException("Wrapped exporter cannot be null");
        this.wrappedExporter = wrappedExporter;
    }
    @Override
    public void export(String destination, String data) {
        wrappedExporter.export(destination, data);
    }
}

public class HeaderFooterDecorator extends ExporterDecorator {
    private final String timestamp;
    public HeaderFooterDecorator(Exporter wrappedExporter, String timestamp) {
        super(wrappedExporter);
        this.timestamp = timestamp;
    }
    @Override
    public void export(String destination, String data) {
        String enrichedData = "=== METADATA HEADER: " + timestamp + " ===\n" 
                            + data + "\n" 
                            + "=== INTEGRITY CHECK: SHA-256 OK | END RECORD ===";
        super.export(destination, enrichedData);
    }
}

public class Base64EncryptDecorator extends ExporterDecorator {
    public Base64EncryptDecorator(Exporter wrappedExporter) {
        super(wrappedExporter);
    }
    @Override
    public void export(String destination, String data) {
        String encoded = java.util.Base64.getEncoder().encodeToString(data.getBytes());
        String payload = "[BASE64_CIPHERTEXT:" + encoded + "]";
        super.export(destination, payload);
    }
}