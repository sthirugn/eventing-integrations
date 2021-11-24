The included are from the Notifications App's backend repo here:
https://github.com/RedHatInsights/notifications-backend/tree/master/backend/helpers

This specific example has pre-filled the bundle/app/event_type as rhel/advisor/new-recommendations, but you could also set this as rhel/drift/baseline-drift, for example.

Once the Notifications App's settings account for the Splunk sub_type, or possibly implement partial default behaviour groups, this setup script will likely change or be replaced by the Notifications' default script.
See NOTIF-392 to learn more: https://issues.redhat.com/browse/NOTIF-392

Running the script without options will print out its usage.
