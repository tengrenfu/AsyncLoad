# AsyncLoad
Asynchronous loads content and images with JSON and display them onto listview 


How to load the project with your Eclipse on your local

1) File -> Import -> General -> Existing project into worksapce ->
Select root directory -> Browse...
then you select the root directory of this project -> Finish

2) If you get errors on SDK version, please modify the value of 
'android:targetSdkVersion' according to your API Level in AndroidManifest.xml ( Please right click the project -> Properties -> 
click 'Android' on left side then you will find the the value of "API Level" in 'Project Build Target" that you had selected ).

3) Import gson library in your build path, please right click the project -> Properties -> click 'Java Build Path' on the left side then click 'Libraries' -> Add External JARs... -> please select the gson-2.1.jar in 'libs' directory of this project -> then click the tap 'Order and Export' and check the gson-2.1.jar you just loaded -> click 'OK' button

4) Now you may build the project and run the application.