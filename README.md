# rewards-engine
Sample project to get knowage around kafka streams

To triger the full processing you need to send messages like this one: 

topic: users

{
  "tableName": "user",
  "operationType" : "u",
  "before": {
    "id" : 131,
    "inserted" : 1,
    "isSpecial" : true,
    "mail" : "mail@mail.mail"
  },
  "after": {
    "id" : 131,
    "inserted" : 1,
    "isSpecial" : true,
    "mail" : "mail@mail.mail"
  }
}

topic: campaign

{
  "id" : 123,
  "lastChangeTimeInstance" : 1,
  "from": 2,
  "to":3,
  "type":"n",
  "calculationBase": "10.1",
  "calculationType": "fee"
}

topic: payments 

{
  "tableName": "trn",
  "operationType" : "u",
  "before": {
    "type" : "n",
    "saleId" : 87,
    "id": 1,
    "amount": "10.1"
  },
  "after": {"id": 1,
    "type" : "n",
    "saleId" : 87,
    "userId": 131,
    "status": 3,
    "isFirst": true,
    "amount": "10.1"
  }
}
