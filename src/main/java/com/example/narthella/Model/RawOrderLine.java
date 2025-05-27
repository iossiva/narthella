package com.example.narthella.Model;

import lombok.Data;

import java.util.Map;


public class RawOrderLine {

    private Map<String,Object> raw;
    private Map<String,Object> converted;

    public Map<String, Object> getRaw() {
        return raw;
    }

    public void setRaw(Map<String, Object> raw) {
        this.raw = raw;
    }

    public Map<String, Object> getConverted() {
        return converted;
    }

    public void setConverted(Map<String, Object> converted) {
        this.converted = converted;
    }
}
