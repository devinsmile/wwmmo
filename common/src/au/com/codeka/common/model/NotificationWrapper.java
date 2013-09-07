// Code generated by Wire protocol buffer compiler, do not edit.
// Source file: ./messages.proto
package au.com.codeka.common.model;

import com.squareup.wire.ByteString;
import com.squareup.wire.Message;
import com.squareup.wire.ProtoField;
import java.util.Collections;
import java.util.List;

import static com.squareup.wire.Message.Datatype.BYTES;
import static com.squareup.wire.Message.Label.REPEATED;
import static com.squareup.wire.Message.Label.REQUIRED;

public final class NotificationWrapper extends Message {

  public static final ByteString DEFAULT_ORIGINAL_MESSAGE = ByteString.EMPTY;
  public static final List<Notification> DEFAULT_NOTIFICATIONS = Collections.emptyList();

  @ProtoField(tag = 1, type = BYTES, label = REQUIRED)
  public ByteString original_message;

  @ProtoField(tag = 2, label = REPEATED)
  public List<Notification> notifications;

  private NotificationWrapper(Builder builder) {
    super(builder);
    this.original_message = builder.original_message;
    this.notifications = immutableCopyOf(builder.notifications);
  }

  @Override
  public boolean equals(Object other) {
    if (other == this) return true;
    if (!(other instanceof NotificationWrapper)) return false;
    NotificationWrapper o = (NotificationWrapper) other;
    return equals(original_message, o.original_message)
        && equals(notifications, o.notifications);
  }

  @Override
  public int hashCode() {
    int result = hashCode;
    if (result == 0) {
      result = original_message != null ? original_message.hashCode() : 0;
      result = result * 37 + (notifications != null ? notifications.hashCode() : 0);
      hashCode = result;
    }
    return result;
  }

  public static final class Builder extends Message.Builder<NotificationWrapper> {

    public ByteString original_message;
    public List<Notification> notifications;

    public Builder() {
    }

    public Builder(NotificationWrapper message) {
      super(message);
      if (message == null) return;
      this.original_message = message.original_message;
      this.notifications = copyOf(message.notifications);
    }

    public Builder original_message(ByteString original_message) {
      this.original_message = original_message;
      return this;
    }

    public Builder notifications(List<Notification> notifications) {
      this.notifications = notifications;
      return this;
    }

    @Override
    public NotificationWrapper build() {
      checkRequiredFields();
      return new NotificationWrapper(this);
    }
  }
}
