package com.zecovery.android.nochedigna.albergue;

/**
 * Created by fran on 10-07-16.
 */

public class Albergue {

    private String idAlbergue;
    private String region;
    private String comuna;
    private String tipo;
    private String cobertura;
    private String camasDisponibles;
    private String ejecutor;
    private String direccion;
    private String telefonos;
    private String email;
    private String lat;
    private String lng;

    public Albergue(String idAlbergue, String region, String comuna, String tipo, String cobertura, String camasDisponibles, String ejecutor, String direccion, String telefonos, String email, String lat, String lng) {
        this.idAlbergue = idAlbergue;
        this.region = region;
        this.comuna = comuna;
        this.tipo = tipo;
        this.cobertura = cobertura;
        this.camasDisponibles = camasDisponibles;
        this.ejecutor = ejecutor;
        this.direccion = direccion;
        this.telefonos = telefonos;
        this.email = email;
        this.lat = lat;
        this.lng = lng;
    }

    public Albergue() {
    }

    public String getIdAlbergue() {
        return idAlbergue;
    }

    public void setIdAlbergue(String idAlbergue) {
        this.idAlbergue = idAlbergue;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getComuna() {
        return comuna;
    }

    public void setComuna(String comuna) {
        this.comuna = comuna;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getCobertura() {
        return cobertura;
    }

    public void setCobertura(String cobertura) {
        this.cobertura = cobertura;
    }

    public String getCamasDisponibles() {
        return camasDisponibles;
    }

    public void setCamasDisponibles(String camasDisponibles) {
        this.camasDisponibles = camasDisponibles;
    }

    public String getEjecutor() {
        return ejecutor;
    }

    public void setEjecutor(String ejecutor) {
        this.ejecutor = ejecutor;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getTelefonos() {
        return telefonos;
    }

    public void setTelefonos(String telefonos) {
        this.telefonos = telefonos;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getLat() {
        return lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public String getLng() {
        return lng;
    }

    public void setLng(String lng) {
        this.lng = lng;
    }
}
