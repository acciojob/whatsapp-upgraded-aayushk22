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

    public WhatsappRepository() {
        userHashMap = new HashMap<>();
        groupAdminDB = new HashMap<>();
        messageHashMap = new HashMap<>();
        groupUsersDb = new HashMap<>();
        groupToMessagesDb = new HashMap<>();
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

        List<Message> messageList = groupToMessagesDb.get(group);
        messageList.add(message);
        groupToMessagesDb.put(group,messageList);
        return groupToMessagesDb.get(group).size();
    }
}
