 module ILEP { //javafx on laptop, ILEP when desktop
 	requires javafx.base;
 	requires javafx.controls;
 	requires javafx.graphics;
 	requires javafx.media;
 	requires javafx.fxml;
	requires java.desktop;

 	exports models;
 	exports tests;
 	exports views;
 	exports flowLogic;
// 	exports appData;
// 	exports data;

 }