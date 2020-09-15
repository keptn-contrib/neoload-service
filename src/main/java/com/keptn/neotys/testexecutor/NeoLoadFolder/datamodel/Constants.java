package com.keptn.neotys.testexecutor.NeoLoadFolder.datamodel;

public class Constants {
    private String  name;
    private String value;

	public Constants() {
	}

	public Constants(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "Constants{" +
                "name=" + name +
                " value ="+ value+
                '}';
    }
}
