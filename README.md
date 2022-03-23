# Distributed Ledger

## /clones

Get clones list from node

HTTP method: GET

Request parameters:
- ip = requester node's ip
- port = requester node's port
  Response body: List<Clone>
  Content-Type: application/json

## /transaction/push

Push transaction to node. Then it turns to block and is added to ledger. 
HTTP method: POST

Request Body: {"transaction": "string"}

Response Body: Text explaining status ("success"/"failure")

Content-Type: application/json

## /blocks/push

Used to push block from one node to another

HTTP method: POST

Request Body: {"hash": "string", "transaction": "string"}

Response Body: No body

## /blocks/get

Sends node's block list to receiver. Used to synchronize ledger

HTTP method: GET

Response body: List<Block>

Content-Type: application/json