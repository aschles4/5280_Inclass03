The API implementation was a serverless implementation.  (WARNING: When hitting the API the endpoints take time to warmup if the API has not been hit recently )

The API uses AWS API Gateway to create endpoints that trigger one of five POST lambda functions. 
	/login
	/logout
	/signup
	/findProfile
	/editProfile


The android app is also included
