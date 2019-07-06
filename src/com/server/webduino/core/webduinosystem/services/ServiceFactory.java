package com.server.webduino.core.webduinosystem.services;

import com.server.webduino.core.webduinosystem.services.Service;
import com.server.webduino.core.webduinosystem.services.VoipService;

/**
 * Created by giaco on 12/05/2017.
 */
public class ServiceFactory {

    public Service createService(int id, String name, String type, String param) {
        Service service = null;
        if (type.equals("voip")) {
            service = new VoipService(id,name,type,param);
        } else if (type.equals("alarmnotification")) {
            service = new AlarmNotificationService(id,name,type,param);
        } else if (type.equals("androidnotification")) {
            service = new AndroidNotificationService(id,name,type,param);
        } else if (type.equals("sms")) {
            service = new SMSService(id,name,type,param);
        } else if (type.equals("tts")) {
            service = new TTSService(id,name,type,param);
        } else {
            service = new Service(id,name,type,param);
        }
        if (service != null) {
            service.init();
        }
        return service;
    }
}
