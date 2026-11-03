package task3;

public interface DispatchStrategy {
    boolean shouldDispatch(int severityLevel, int currentHour);
    String getStrategyName();
}

public class ImmediateStrategy implements DispatchStrategy {
    @Override
    public boolean shouldDispatch(int severityLevel, int currentHour) { return true; }
    @Override
    public String getStrategyName() { return "IMMEDIATE_DISPATCH"; }
}

public class BusinessHoursStrategy implements DispatchStrategy {
    @Override
    public boolean shouldDispatch(int severityLevel, int currentHour) {
        if (severityLevel >= 5) return true;
        return (currentHour >= 9 && currentHour <= 17);
    }
    @Override
    public String getStrategyName() { return "BUSINESS_HOURS_ONLY (09:00-17:00)"; }
}

public interface MessageChannel {
    void transmit(String recipient, String message);
}

public class EmailChannel implements MessageChannel {
    @Override
    public void transmit(String recipient, String message) {
        System.out.printf("[EMAIL PROTOCOL -> %s] %s%n", recipient, message);
    }
}

public class SmsChannel implements MessageChannel {
    @Override
    public void transmit(String recipient, String message) {
        String condensed = message.replace("\n", " -- ");
        System.out.printf("[SMS PROTOCOL -> %s] %s%n", recipient, condensed);
    }
}

public abstract class Alert {
    protected final MessageChannel channel;
    protected final DispatchStrategy strategy;
    public Alert(MessageChannel channel, DispatchStrategy strategy) {
        if (channel == null || strategy == null) throw new IllegalArgumentException("Dependencies cannot be null");
        this.channel = channel;
        this.strategy = strategy;
    }
    public void processAlert(String recipient, String rawContent, int currentHour) {
        int severity = getSeverityLevel();
        if (strategy.shouldDispatch(severity, currentHour)) {
            String formatted = formatAlertBody(rawContent);
            channel.transmit(recipient, formatted);
        } else {
            System.out.printf("[DISPATCH SUPPRESSED] Alert blocked by policy '%s' at hour %02d:00 (Severity: %d)%n",
                              strategy.getStrategyName(), currentHour, severity);
        }
    }
    public abstract int getSeverityLevel();
    public abstract String formatAlertBody(String raw);
}

public class CriticalSystemAlert extends Alert {
    public CriticalSystemAlert(MessageChannel channel, DispatchStrategy strategy) {
        super(channel, strategy);
    }
    @Override
    public int getSeverityLevel() { return 5; }
    @Override
    public String formatAlertBody(String raw) {
        return "!!! CRITICAL PRODUCTION ALERT !!!\n"
             + "Incident Report: " + raw + "\n"
             + "ACTION REQUIRED: SRE on-call immediate intervention.";
    }
}

public class DailySummaryAlert extends Alert {
    public DailySummaryAlert(MessageChannel channel, DispatchStrategy strategy) {
        super(channel, strategy);
    }
    @Override
    public int getSeverityLevel() { return 1; }
    @Override
    public String formatAlertBody(String raw) {
        return "Daily Digest:\n" + raw;
    }
}