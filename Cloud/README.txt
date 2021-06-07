This is the step to deploy model in Cloud Function.

setting roles in cloud IAM.
create firewall.
setting alerting & budgeting.
create monitoring (easy to see logs etc.).
create bucket in cloud storage (for storing dataset, android input, etc.)
build VM machine in AI PLATFORM.
training model in VM.
save model in bucket.

"how our team deploy Ml in GCP".
android input save in bucket. 
create trigger in cloud function.
create prediction in cloud function. (cloud function code can see in ML files).
create trigger in cloud function to send the prediction to firestore so android can pull.
