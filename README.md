# BlockFence server demo

## Notes:
   * we are using infura in order to connect to eth mainnet (alternative to full node)

## endpoints:

   * GET `/v1/eth/version` will return the version of web3j client
     
   * GET `/v1/eth/ctrawdata?address={contract address}` will return the byteCode from address


## swagger endpoint:
you can test the api using the following link(instead of postman):
http://localhost:8080/swagger-ui.html

## run:
   run the Attached dockerfile (publish port 8080)


# Deployment

## Heroku url
swagger: 
https://blockfence-demo.herokuapp.com/swagger-ui.html


