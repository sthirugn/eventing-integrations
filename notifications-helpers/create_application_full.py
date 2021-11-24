import helpers

base_url = "http://localhost:8085"
tp_part = ""  # set to /api/notifications if going via TurnPike

helpers.set_path_prefix(base_url + tp_part)

# Parameters to set
bundle_name = "rhel"
bundle_description = "My RHEL system"
app_name = "advisor"
app_display_name = "advisor"
event_type = "new-recommendation"
event_type_display_name = "Advisor Recommendations"
bg_name = "Send Advisor Recommendations to Splunk"

f = open("rhid.txt", "r")

line = f.readline()
# strip eventual \n at the end
x_rh_id = line.strip()
f.close()

# ---
# Add the application

print(">>> create bundle")
bundle_id = helpers.add_bundle(bundle_name, bundle_description)

print(">>> create application")
app_id = helpers.add_application(bundle_id, app_name, app_display_name)

print(">>> add eventType to application")
et_id = helpers.add_event_type(app_id, event_type, event_type_display_name)

print(">>> create a behavior group")
bg_id = helpers.create_behavior_group(bg_name, bundle_id, x_rh_id )

print(">>> add event type to behavior group")
helpers.add_event_type_to_behavior_group(et_id, bg_id, x_rh_id)

print(">>> create splunk endpoint")
props = {
    "url": "https://my_splunk_hec.splunkcloud.com",
    "sub_type": "splunk",
    "extras": {
        "token": "SPLUNK_TOKEN"
    }
}
ep_id = helpers.create_endpoint("splunk", x_rh_id, props, "camel")

print(">>> link splunk endpoint to behaviour group")
helpers.link_bg_endpoint(bg_id, ep_id, x_rh_id)
