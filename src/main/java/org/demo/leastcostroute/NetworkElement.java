package org.demo.leastcostroute;

public class NetworkElement {
    String name;
    int processingTime;
    String exchange;

    public NetworkElement(String name, int processingTime, String exchange) {
        this.name = name;
        this.processingTime = processingTime;
        this.exchange = exchange;
    }
}
