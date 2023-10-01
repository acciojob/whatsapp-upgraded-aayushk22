package com.driver;

import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class WhatsappService {
    WhatsappRepository whatsappRepository = new WhatsappRepository();


    public String createUser(String name, String mobile) {
        try{
            String response = whatsappRepository.createUser(name,mobile);
            return response;
        }
        catch (Exception e) {
            return e.getMessage();
        }
    }

    public Group createGroup(List<User> users) {
        Group group = whatsappRepository.createGroup(users);
        return group;
    }

    public int createMessage(String content) {
        int response = whatsappRepository.createMessage(content);
        return response;
    }

    public int sendMessage(Message message, User sender, Group group) {

        int response = whatsappRepository.sendMessage(message,sender,group);
        return response;

    }

    public String changeAdmin(User approver, User user, Group group) {
        try{
            String response = whatsappRepository.changeAdmin(approver,user,group);
            return response;
        }
        catch (Exception e) {
            return e.getMessage();
        }
    }

    public int removeUser(User user) {

//        int response = whatsappRepository.removeUser(user);
//        return response;
        return 0;
    }

    public String findMessage(Date start, Date end, int k) {
        return "";
    }
}
