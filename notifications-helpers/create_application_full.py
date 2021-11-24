import helpers

base_url = "http://localhost:8085"
tp_part = ""  # set to /api/notifications if going via TurnPike

helpers.set_path_prefix(base_url + tp_part)

# Parameters to set
bundle_name = "rhel"
app_name = "advisor"
event_type = "new-recommendation"
bg_name = "Send Advisor Recommendations to Splunk"

f = open("rhid.txt", "r")

line = f.readline()
# strip eventual \n at the end
x_rh_id = line.strip()
f.close()


bundle_id = helpers.find_bundle(bundle_name)
print(f"Bundle: {bundle_name} {bundle_id}")
if not bundle_id:
    exit('missing bundle')

app_id = helpers.find_application(bundle_id, app_name)
print(f"Application: {app_name} {app_id}")
if not app_id:
    exit('missing application')

et_id = helpers.find_event_type(app_id, event_type)
print(f"Event Type: {event_type} {et_id}")
if not et_id:
    exit('missing event type')

print(f">>> create a behavior group {bg_name}")
bg_id = helpers.create_behavior_group(bg_name, bundle_id, x_rh_id)

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
