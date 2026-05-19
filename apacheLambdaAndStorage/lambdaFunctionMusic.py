import boto3
from boto3.dynamodb.conditions import Attr

def lambda_handler(event, context):
    client = boto3.resource("dynamodb")
    table = client.Table("music")

    type = event["type"]
    resp = "null"

    if (type == "list"):  # for getting music subbed lis
        idArray = event["musicid"].split(',')
        idObjArray = []

        haveStuffs = False
        # print(idArray)
        print(len(idArray))
        if (len(idArray) == 1):
            # check if the 1 element is empty or not
            if (idArray[0] != ''):
                haveStuffs = True
        elif (len(idArray) > 1):
            haveStuffs = True
        
        if haveStuffs:
            for i in idArray:
                idObjArray.append({"id": int(i)})

        posted = None
        if haveStuffs:
            posted = client.batch_get_item(
                RequestItems={
                    "music": {
                        "Keys": idObjArray
                    }
                }
            )
        resp = posted
    elif (type == "search"): # for getting music search result
        title = event["title"]
        artist = event["artist"]
        year = event["year"]

        filterList = []
        if (title != ""):
            filterList.append(Attr("title").eq(title.title()))
        if (artist != ""):
            filterList.append(Attr("artist").eq(artist.title()))
        if (year != ""):
            filterList.append(Attr("year").eq(int(year)))

        goAhead = False
        
        if len(filterList) == 1:
            filterExp = filterList[0]
            goAhead = True
        elif len(filterList) == 2:
            filterExp = filterList[0] & filterList[1]
            goAhead = True
        elif len(filterList) == 3:
            filterExp = filterList[0] & filterList[1] & filterList[2]
            goAhead = True

        if (goAhead):
            resp = table.scan( FilterExpression=filterExp )


    return resp
    