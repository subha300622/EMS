package com.example.ems.support.dto;

public class PlatformAgentResponse {

    private String id;
    private String name;
    private int ticketsAssigned;
    private String status;

    public PlatformAgentResponse() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getTicketsAssigned() { return ticketsAssigned; }
    public void setTicketsAssigned(int ticketsAssigned) { this.ticketsAssigned = ticketsAssigned; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
