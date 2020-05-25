# EasyDataLabeler
An android studio project and also the finished APK file for the app to label image data for ML applications on an android device and store the label file in the required XML format

# How to install :-
This project consists of an android app that can be deployed by simply installing from the apk file. Ensure that the ‘Install from unknown sources’ setting is kept ‘ON’ on the device. If not, then you might receive a message saying ‘Allow installation from unknown sources’. Select ‘Yes’ from the same. This will start the app.

The github link to the apk file as well as the android studio project is as follows:-


# Project :-
The code and development has been done on Android Studio IDE. I have uploaded the project on github. Link to the same is as follows :-  

# Features :-
Currently, the app allows you to Sign up or make an account as a Data scientist or a Data labeler.
Once created an account, you can login into your account. The account details database is stored on the app server which is deployed on firebase. If the credentials are correct, the account page is shown.
If the account type is ‘Data Scientist’, the Project list appears, where the user can. 
Select a project and upload data images to it.
Create a new project. 
Download the label HTML files from any project.
If the account type is ‘Data  Labeler’, the user can :- 
Navigate through projects, select a data image and label it using rectangle and polygon draw tools that have been deployed currently. After labeling, the label file is stored in the required HTML format which can be deceived by the data scientist.







A proper guide through the application is given below using the screenshots from the app :



             
 


Future scope/enhancements :-
Many enhancements can be made, some of which are :-
Allowing different levels of accessibility for a project like whether to keep it as open/private such that it is accessible to only some community of people.
Integrating monetary pay to the labelers directly on the basis of how many data images they have labeled. The data uploaders can customize their individual rate card/ rules for the pay.
Data images of a particular project can be arranged on the basis of their difficulty level. The labelers with less expertise can be given access to easir images and the difficulty can be increased thereby. Also verification tasks can be given to some section of users.
Interface and UI can be highly improved. Actually, this is just a prototype version of what I had thought, I didn’t get time to work on the UI.
Filter images on the basis of how many times they have been labeled.
Allow labeling the same image to multiple labelers and the label which is more overlapped among all the labels can be considered.
Same idea can be extended to audio, text, video annotations for NLP based models.
