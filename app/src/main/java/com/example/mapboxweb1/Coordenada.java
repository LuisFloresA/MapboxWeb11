package com.example.mapboxweb1;

import java.util.ArrayList;

public class Coordenada {
    String nombre;
    float latitud;
    float longitud;
    String desc;
    String diasSema;
    String categoria;
    String [] dias;

    public Coordenada(String nombre,String desc, float latitud, float longitud, String diasSemana,String categoria) {
        this.nombre = nombre;
        this.desc = desc;
        this.latitud = latitud;
        this.longitud = longitud;
        this.diasSema = diasSemana;
        this.categoria = categoria;
        stringToArray(diasSemana);

    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public String getDiasSema() {
        return diasSema;
    }

    public String[] getDias() {
        return dias;
    }

    public void setDias(String[] dias) {
        this.dias = dias;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public void stringToArray(String key){
        dias = key.split(",");
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public float getLatitud() {
        return latitud;
    }

    public void setLatitud(float latitud) {
        this.latitud = latitud;
    }

    public float getLongitud() {
        return longitud;
    }

    public void setLongitud(float longitud) {
        this.longitud = longitud;
    }
}
