package common.dto;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import server.domain.BackpackItem;
import server.domain.InventoryItem;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

@Component
public class UserProfileStructure {

    public int id;

    @Value("#{empty}")
    public String name;

    @Value("#{1}")
    public int level;

    @Value("#{0}")
    public int experience;

    @Value("#{25}")
    public int energy;

    @Value("#{0}")
    public int rating;

    @Value("#{100}")
    public int money;

    @Value("#{emptyBackpackList}")
    public BackpackItem[] backpack;

    @Value("#{emptyInventoryItemList}")
    public InventoryItem[] inventory;

    @Value("#{emptyFriendsList}")
    public int[] friends;

    @Value("#{empty}")
    private String emptyCollection;

    @Override
    public String toString() {
        return "UserProfileStructure{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", level=" + level +
                ", experience=" + experience +
                ", energy=" + energy +
                ", rating=" + rating +
                ", money=" + money +
                ", backpack=" + Arrays.toString(backpack) +
                ", inventory=" + Arrays.toString(inventory) +
                ", friends=" + Arrays.toString(friends) +
                '}';
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

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

    public BackpackItem[] getBackpack() {
        return backpack;
    }

    public void setBackpack(BackpackItem[] backpack) {
        this.backpack = backpack;
    }

    public InventoryItem[] getInventory() {
        return inventory;
    }

    public void setInventory(InventoryItem[] inventory) {
        this.inventory = inventory;
    }

    public int[] getFriends() {
        return friends;
    }

    public void setFriends(int[] friends) {
        this.friends = friends;
    }

    public String getEmptyCollection() {
        return emptyCollection;
    }

    public void setEmptyCollection(String emptyCollection) {
        this.emptyCollection = emptyCollection;
    }
}
