package dte.masteriot.asp.aquaponics;

public class MSG {
    private String type;        //Alarm or Report
    private String text;        //text
    private String originator;  //FishTank or Plant

    MSG(String originator, String type, String text){
        this.originator = originator;
        this.type = type;
        this.text = text;
    }

    public String getOriginator() {
        return originator;
    }

    public String getType() {
        return type;
    }

    public String getText() {
        return text;
    }
}
