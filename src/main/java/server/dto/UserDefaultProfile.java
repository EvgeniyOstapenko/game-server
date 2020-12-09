package server.dto;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import server.domain.BackpackItem;
import server.domain.InventoryItem;

import java.util.List;
import java.util.Set;

@Component
public class UserDefaultProfile {

    @Value("#{empty}")
    private String name;

    @Value("#{1}")
    private int level;

    @Value("#{0}")
    private int experience;

    @Value("#{25}")
    private int energy;

    @Value("#{0}")
    private int rating;

    @Value("#{100}")
    private int money;

    @Value("#{emptyBackpackList}")
    private List<BackpackItem> backpack;

    @Value("#{emptyInventoryItemList}")
    private List<InventoryItem> inventory;

    @Value("#{emptyFriendsList}")
    private Set<Integer> friends;

    @Value("#{empty}")
    private String emptyCollection;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getExperience() {
        return experience;
    }

    public void setExperience(int experience) {
        this.experience = experience;
    }

    public int getEnergy() {
        return energy;
    }

    public void setEnergy(int energy) {
        this.energy = energy;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public int getMoney() {
        return money;
    }

    public void setMoney(int money) {
        this.money = money;
    }

    public List<BackpackItem> getBackpack() {
        return backpack;
    }

    public void setBackpack(List<BackpackItem> backpack) {
        this.backpack = backpack;
    }

    public List<InventoryItem> getInventory() {
        return inventory;
    }

    public void setInventory(List<InventoryItem> inventory) {
        this.inventory = inventory;
    }

    public Set<Integer> getFriends() {
        return friends;
    }

    public void setFriends(Set<Integer> friends) {
        this.friends = friends;
    }

    public String getEmptyCollection() {
        return emptyCollection;
    }

    public void setEmptyCollection(String emptyCollection) {
        this.emptyCollection = emptyCollection;
    }
}
