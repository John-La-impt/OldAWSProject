import boto3
# from boto3.dynamodb.conditions import Attr
def lambda_handler(event, context):
    client = boto3.resource("dynamodb")
    table = client.Table("login") # get table name here

    type = event["type"] # login | register | editMusicSub | getUser
    resp = "null"

    if type == "getUser":
        userEmail = event["userEmail"]
        resp = table.get_item(Key={'email': userEmail})
    elif type == "editMusicSub":
        userEmail = event["userEmail"]
        newMusicSub = event["newMusicSub"]
        resp = table.update_item(
            Key={"email": userEmail},
            UpdateExpression="set musicsub=:r",
            ExpressionAttributeValues={":r": newMusicSub},
            ReturnValues="UPDATED_NEW",
        )
    elif type=="login": # GET access here - task 3
        email = event['email'] # Email is key attribute
        password = event['password']
        db_value=table.get_item(Key={'email': email})

        returnStatus = 300
        if 'Item' in db_value: # key (email) exists
            gotEmail = db_value['Item']['email']
            gotPassword = db_value['Item']['password']

            passwordCheck = False
            if password == gotPassword:
                returnStatus = 200
                passwordCheck = True
            else:
                returnStatus = 250

            resp = {
                'statusCode': returnStatus,
                'headers': {
                    'Access-Control-Allow-Headers': 'Content-Type',
                    'Access-Control-Allow-Origin': '*',
                    'Access-Control-Allow-Methods': '*'
                },
                'body': {  # the body should contain something that signifies that the Email + password combo was correct or not
                    'passwordCorrect': passwordCheck,
                }
            }
        else:
            resp = {
                'statusCode': returnStatus,
                'headers': {
                    'Access-Control-Allow-Headers': 'Content-Type',
                    'Access-Control-Allow-Origin': '*',
                    'Access-Control-Allow-Methods': '*'
                },
                'body': {  # the body should contain something that signifies that the Email + password combo was correct or not
                    'stuffs': 'Not Exists',
                }
            }
    elif type=="register":
        email = event["email"] # Email is key attribute
        password = event["password"]
        username = event["username"]
        # need to check 1st if email already exists.
        db_value=table.get_item(Key={"email": email})

        returnStatus = 300
        if 'Item' not in db_value: # key email does not exists
            returnStatus = 200
            resp = table.put_item(
                Item = {
                    "email": email,
                    "password": password,
                    "user_name": username,
                    "musicsub": ""
                }
            )
        else:
            resp = {
                'statusCode': returnStatus,
                'headers': {
                    'Access-Control-Allow-Headers': 'Content-Type',
                    'Access-Control-Allow-Origin': '*',
                    'Access-Control-Allow-Methods': '*'
                },
                'body': {  # the body should contain something that signifies that the Email + password combo was correct or not
                    'stuffs': 'Email existed',
                }
            }
    else: # else is options here
        db_value=table.get_item(Key={'Email': email})
        resp = {
         'statusCode': 200,
            'headers': {
                 'Access-Control-Allow-Headers': 'Content-Type',
                'Access-Control-Allow-Origin': '*',
                'Access-Control-Allow-Methods': '*'
            },
            'body': {
                'Count': db_value
            }
        }

    return resp
    