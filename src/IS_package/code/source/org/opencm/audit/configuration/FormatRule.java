package org.opencm.audit.configuration;

public class FormatRule {
    private String rule;
    private String range;
    private String bgcolor;

	public static final String FORMAT_RULE_EQUALS				= "EQUALS";
	public static final String FORMAT_RULE_DIFFERENCES			= "DIFFS";

    public FormatRule() {
    }
    public String getRule() {
        return this.rule;
    }
    public void setRule(String rule) {
        this.rule = rule;
    }
    public String getRange() {
        return this.range;
    }
    public void setRange(String range) {
        this.range = range;
    }
    public String getBGColor() {
        return this.bgcolor;
    }
    public void setBGColor(String bgcolor) {
        this.bgcolor = bgcolor;
    }
}
