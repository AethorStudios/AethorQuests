package com.aethor.aethorquests.model;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;

/**
 * Represents a single objective within a quest
 */
public class Objective {
    private final ObjectiveType type;
    private final String description;
    
    // KILL objective fields
    private EntityType entityType;
    private String mythicMobName;
    private int killAmount;
    
    // TALK objective fields
    private String talkNpcId;
    
    // COLLECT objective fields
    private Material collectMaterial;
    private int collectAmount;
    
    // VISIT objective fields
    private String visitWorld;
    private double visitX;
    private double visitY;
    private double visitZ;
    private double visitRadius;
    
    public Objective(ObjectiveType type, String description) {
        this.type = type;
        this.description = description;
    }
    
    // Getters
    public ObjectiveType getType() {
        return type;
    }
    
    public String getDescription() {
        return description;
    }
    
    // KILL objective methods
    public EntityType getEntityType() {
        return entityType;
    }
    
    public void setEntityType(EntityType entityType) {
        this.entityType = entityType;
    }
    
    public String getMythicMobName() {
        return mythicMobName;
    }
    
    public void setMythicMobName(String mythicMobName) {
        this.mythicMobName = mythicMobName;
    }
    
    public int getKillAmount() {
        return killAmount;
    }
    
    public void setKillAmount(int killAmount) {
        this.killAmount = killAmount;
    }
    
    public boolean isMythicMob() {
        return mythicMobName != null && !mythicMobName.isEmpty();
    }
    
    // TALK objective methods
    public String getTalkNpcId() {
        return talkNpcId;
    }
    
    public void setTalkNpcId(String talkNpcId) {
        this.talkNpcId = talkNpcId;
    }
    
    // COLLECT objective methods
    public Material getCollectMaterial() {
        return collectMaterial;
    }
    
    public void setCollectMaterial(Material collectMaterial) {
        this.collectMaterial = collectMaterial;
    }
    
    public int getCollectAmount() {
        return collectAmount;
    }
    
    public void setCollectAmount(int collectAmount) {
        this.collectAmount = collectAmount;
    }
    
    // VISIT objective methods
    public String getVisitWorld() {
        return visitWorld;
    }
    
    public void setVisitWorld(String visitWorld) {
        this.visitWorld = visitWorld;
    }
    
    public double getVisitX() {
        return visitX;
    }
    
    public void setVisitX(double visitX) {
        this.visitX = visitX;
    }
    
    public double getVisitY() {
        return visitY;
    }
    
    public void setVisitY(double visitY) {
        this.visitY = visitY;
    }
    
    public double getVisitZ() {
        return visitZ;
    }
    
    public void setVisitZ(double visitZ) {
        this.visitZ = visitZ;
    }
    
    public double getVisitRadius() {
        return visitRadius;
    }
    
    public void setVisitRadius(double visitRadius) {
        this.visitRadius = visitRadius;
    }
    
    public Location getVisitLocation(org.bukkit.Server server) {
        if (visitWorld == null) return null;
        org.bukkit.World world = server.getWorld(visitWorld);
        if (world == null) return null;
        return new Location(world, visitX, visitY, visitZ);
    }
}
