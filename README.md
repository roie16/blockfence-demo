# BlockFence server demo

## swagger endpoint:
you can see and test the api using the following link(instead of postman): http://localhost:8080/swagger-ui.html

## run:
* pre requisite :
  * Java 17 + 
  * Docker (Optional)
    
run the Attached dockerfile (publish port 8080) or using intellij start the app

# Deployment
app is deployed to heroku, auto-updates when merge to master(Heroku pipeline)

## Heroku url
swagger: 
https://blockfence-demo.herokuapp.com/swagger-ui.html

## Notes:
* we are using infura in order to connect to ethereum mainNet (alternative to running our own full node)
