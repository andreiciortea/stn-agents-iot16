package fr.emse.ci.stn.aacontainer.artifacts.notifications;

import java.util.AbstractQueue;
import java.util.concurrent.ConcurrentLinkedQueue;


public class NotificationQueue {
    
    private static NotificationQueue queue;
    private AbstractQueue<Notification> notificationQueue;

    
    private NotificationQueue() {
        notificationQueue = new ConcurrentLinkedQueue<Notification>();
    }
    
    public static synchronized NotificationQueue getInstance() {
        if (queue == null) {
            queue = new NotificationQueue();
        }
        
        return queue;
    }
    
    public Notification poll() {
        return notificationQueue.poll();
    }
    
    public boolean add(Notification notification) {
        return notificationQueue.add(notification);
    }
}
