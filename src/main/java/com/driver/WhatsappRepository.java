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

    HashMap<User,List<Message>> userToMessagesDb;

    public WhatsappRepository() {
        userHashMap = new HashMap<>();
        groupAdminDB = new HashMap<>();
        messageHashMap = new HashMap<>();
        groupUsersDb = new HashMap<>();
        groupToMessagesDb = new HashMap<>();
        userToMessagesDb = new HashMap<>();
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

        //getting the list of messages of a user and adding the new message to that list
        List<Message> userToMessageList = userToMessagesDb.get(sender);
        userToMessageList.add(message);
        userToMessagesDb.put(sender,userToMessageList);

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

        //A user belongs to exactly one group
        //If user is not found in any group, throw "User not found" exception
        User toBeRemoved = null;
        Group userRemovalGrp = null;
        List<User> listForRemoval = null;
        for (Group group: groupUsersDb.keySet()) {
            List<User> listOfUserInGroup = groupUsersDb.get(group);
            for (User u: listOfUserInGroup) {
                if (u == user) {
                    toBeRemoved = u;
                    listForRemoval = listOfUserInGroup;
                    userRemovalGrp = group;
                    break;
                }
            }
        }
        if (toBeRemoved == null) throw new RuntimeException("User not found");

        //If user is found in a group and it is the admin, throw "Cannot remove admin" exception
        if (toBeRemoved == groupAdminDB.get(userRemovalGrp)) throw new RuntimeException("Cannot remove admin");

        //If user is not the admin, remove the user from the group, remove all its messages from all the databases,
        // and update relevant attributes accordingly.
        listForRemoval.remove(toBeRemoved);
        groupUsersDb.put(userRemovalGrp,listForRemoval);

        List<Message> messagesOfUser = userToMessagesDb.get(user);
        userToMessagesDb.put(user,new ArrayList<>()); //emptying all the messages of the user

        List<Message> groupMessageList = groupToMessagesDb.get(userRemovalGrp);
        List<Message> copy = new ArrayList<>();
        copy = groupMessageList;
        for (Message i: copy) {
            for (Message j: messagesOfUser) {
                if(i == j) {
                    groupMessageList.remove(j);
                    continue;
                }
            }
        }

        groupToMessagesDb.put(userRemovalGrp,groupMessageList);

        for (int i: messageHashMap.keySet()) {
            Message message = messageHashMap.get(i);
            for (Message m: messagesOfUser) {
                if (m == message) {
                    messageHashMap.remove(i);
                }
            }
        }

        //If user is removed successfully,
        // return (the updated number of users in the group + the updated number of messages in group + the updated number of overall messages)
        return groupUsersDb.get(userRemovalGrp).size() + groupToMessagesDb.get(userRemovalGrp).size() + messageHashMap.size();
    }
}
