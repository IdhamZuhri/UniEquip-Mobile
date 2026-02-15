package com.example.uniequip;

public class ClubItem {

    public String id;
    public String clubName;
    public String type;
    public String advName;
    public String advNum;
    public String advTel;
    public String advEmail;
    public String status;

    public ClubItem() {}

    public ClubItem(String id, String clubName, String type,
                    String advName, String advNum, String advTel, String advEmail,
                    String status) {
        this.id = id;
        this.clubName = clubName;
        this.type = type;
        this.advName = advName;
        this.advNum = advNum;
        this.advTel = advTel;
        this.advEmail = advEmail;
        this.status = status;
    }
}
