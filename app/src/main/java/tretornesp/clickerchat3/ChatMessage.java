package tretornesp.clickerchat3;

import java.util.Date;

public class ChatMessage {
    private String messageText;
    private String messageUser;
    private String messageName;
    private String isAttachment;
    private String fileSize;
    private String fileType;
    private String fileName;

    public String getIsAttachment() {
        return isAttachment;
    }

    public void setIsAttachment(String isAttachment) {
        this.isAttachment = isAttachment;
    }

    private long messageTime;

    public String getMessageName() {
        return messageName;
    }

    public void setMessageName(String messageName) {
        this.messageName = messageName;
    }

    public ChatMessage(String messageText, String messageUser, String messageName, String isAttachment, String fileSize, String fileType, String fileName) {
        this.messageText = messageText;
        this.messageUser = messageUser;
        this.messageName = messageName;
        this.isAttachment = isAttachment;
        this.fileSize = fileSize;
        this.fileType = fileType;
        this.fileName = fileName;

        messageTime = new Date().getTime();
    }

    public ChatMessage() {

    }

    public String getMessageText() {
        return messageText;
    }

    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }

    public String getMessageUser() {
        return messageUser;
    }

    public void setMessageUser(String messageUser) {
        this.messageUser = messageUser;
    }

    public long getMessageTime() {
        return messageTime;
    }

    public void setMessageTime(long messageTime) {
        this.messageTime = messageTime;
    }
    public String getFileSize() {
        return fileSize;
    }

    public void setFileSize(String fileSize) {
        this.fileSize = fileSize;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}
