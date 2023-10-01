package com.driver;

import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

@Repository
public class WhatsappRepository {
    HashMap<String,User> userHashMap;
    int countOfGroups = 0;

    HashMap<Group,List<User>> groupUsersDb;
    HashMap<Group,User> groupAdminDB;
    HashMap<Integer,Message> messageHashMap;
    HashMap<Group,List<Message>> groupToMessagesDb;

    HashMap<Message,User> senderMap;


    public WhatsappRepository() {
        userHashMap = new HashMap<>();
        groupAdminDB = new HashMap<>();
        messageHashMap = new HashMap<>();
        groupUsersDb = new HashMap<>();
        groupToMessagesDb = new HashMap<>();
        senderMap = new HashMap<>();
    }

    public String createUser(String name, String mobile) {
        if (userHashMap.containsKey(mobile)) {
            throw new RuntimeException("User already exists");
        }

        User user = new User(name,mobile);
        userHashMap.put(mobile,user);
        return "SUCCESS";
    }


    public Group createGroup(List<User> users) {
        if (users.size() == 2) {
            Group group = new Group(users.get(1).getName(),2);
            groupAdminDB.put(group,users.get(0));
            groupUsersDb.put(group,users);
            groupToMessagesDb.put(group,new ArrayList<>());
            return group;
        }
        else {
            this.countOfGroups++;
            Group group = new Group("Group " + countOfGroups,users.size());
            groupAdminDB.put(group,users.get(0));
            groupUsersDb.put(group,users);
            groupToMessagesDb.put(group,new ArrayList<>());
            return group;
        }
    }

    public int createMessage(String content) {
        int countOfMessages = messageHashMap.size();
        int id = countOfMessages+1;
        Message message = new Message(id,content);
        Date currentDate = new Date();
        message.setTimestamp(currentDate);
        messageHashMap.put(id,message);
        return id;
    }

    public int sendMessage(Message message, User sender, Group group) {
        //Throw "Group does not exist" if the mentioned group does not exist
        //Throw "You are not allowed to send message" if the sender is not a member of the group
        //If the message is sent successfully, return the final number of messages in that group.
        if (!groupUsersDb.containsKey(group)) {
            throw new RuntimeException("Group does not exist");
        }

        List<User> userList = groupUsersDb.get(group);
        if (!userList.contains(sender)){
            throw new RuntimeException("You are not allowed to send message");
        }

        //getting the list of messages in the group and adding the message to that list
        List<Message> messageList = groupToMessagesDb.get(group);
        messageList.add(message);
        groupToMessagesDb.put(group,messageList);


        senderMap.put(message,sender);

        return groupToMessagesDb.get(group).size();
    }

    public String changeAdmin(User approver, User user, Group group) {
        //Throw "Group does not exist" if the mentioned group does not exist
        //Throw "Approver does not have rights" if the approver is not the current admin of the group
        //Throw "User is not a participant" if the user is not a part of the group
        //Change the admin of the group to "user" and return "SUCCESS".
        // Note that at one time there is only one admin and the admin rights are transferred from approver to user.

        if (!groupUsersDb.containsKey(group)) {
            throw new RuntimeException("Group does not exist");
        }

        if (groupAdminDB.get(group) != approver) {
            throw new RuntimeException("Approver does not have rights");
        }

        if (!groupUsersDb.get(group).contains(user)) {
            throw new RuntimeException("User is not a participant");
        }

        User currentAdmin = groupAdminDB.get(group);
        groupAdminDB.put(group,user);
        return "SUCCESS";
    }


    public int removeUser(User user) {
        Boolean flag = false;

        List<Message> messageList = new ArrayList<>();
        for (Message message : senderMap.keySet()){
            if (senderMap.get(message).equals(user)){
                messageList.add(message);
            }
        }
        for (Message message : messageList){
            if (senderMap.containsKey(message)){
                senderMap.remove(message);
            }
        }

        Group userGroup = null;

        for (Group group: groupUsersDb.keySet()) {
            if (groupAdminDB.get(group).equals(user)) {
                throw new RuntimeException("Cannot remove admin");
            }

            List<User> userList = groupUsersDb.get(group);
            if (userList.contains(user)) {
                flag = true;
                userGroup = group;
                userList.remove(user);
            }
        }

        if (flag == false){
            throw new RuntimeException("User not found");
        }

        List<Message> messages = groupToMessagesDb.get(userGroup);
        for (Message message: messageList) {
            if (messages.contains(message)){
                messages.remove(message);
            }
        }



        return groupUsersDb.get(userGroup).size() + groupToMessagesDb.get(userGroup).size() + senderMap.size();
    }
}
