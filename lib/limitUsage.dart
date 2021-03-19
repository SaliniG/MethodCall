

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_local_notifications/flutter_local_notifications.dart';
import 'package:phone_usage_app/main.dart';

class LimitScreen extends StatefulWidget {

  @override
  _CountDownTimerState createState() => _CountDownTimerState();
}

class _CountDownTimerState extends State<LimitScreen>
    with TickerProviderStateMixin {
  bool _isEditingText = false;
  TextEditingController _editingController;
  String initialText="0.0";

  AnimationController controller;

  static const platform = const MethodChannel('flutter.native/helper');
  void _getLimitUsage() async {
    try {
      final String result = await platform.invokeMethod('_getlimitUsage',{"text":initialText});
      print(result);
    } on PlatformException catch (e) {
      initialText = "Failed to get battery level: '${e.message}'.";
    }

  }
  FlutterLocalNotificationsPlugin flutterLocalNotificationsPlugin = new FlutterLocalNotificationsPlugin();

  showNotification() async {
    var time = new Time(3, 51, 0); //at 3.30
    var androidPlatformChannelSpecifics =
    new AndroidNotificationDetails('repeatDailyAtTime channel id',
        'repeatDailyAtTime channel name', 'repeatDailyAtTime description');
    var iOSPlatformChannelSpecifics =
    new IOSNotificationDetails();
    var platformChannelSpecifics = new NotificationDetails(
        androidPlatformChannelSpecifics, iOSPlatformChannelSpecifics);
    await flutterLocalNotificationsPlugin.showDailyAtTime(
        0,
        'Teer Result Time',
        'Open The App and check for the Result',
        time,
        platformChannelSpecifics);
  }
  String get timerString {
    Duration duration = controller.duration * controller.value;
    return '${duration.inMinutes}:${(duration.inSeconds % 60).toString().padLeft(2, '0')}';
  }

  @override
  void initState() {
    super.initState();
    _editingController = TextEditingController(text: initialText);
    controller = AnimationController(
      vsync: this,
      duration: Duration(seconds: 5),
    );
    showNotification();
  }

  @override
  Widget build(BuildContext context) {
    ThemeData themeData = Theme.of(context);

    return Scaffold(
      backgroundColor: Colors.white10,
      body: AnimatedBuilder(
          animation: controller,
          builder: (context, child) {
            return Stack(
              children: <Widget>[
                Padding(
                  padding: EdgeInsets.all(8.0),
                  child: Column(
                    mainAxisAlignment: MainAxisAlignment.spaceBetween,
                    children: <Widget>[
                      Expanded(
                        child: Align(
                          alignment: FractionalOffset.center,
                          child: AspectRatio(
                            aspectRatio: 1.0,
                            child: Stack(
                              children: <Widget>[
                                Positioned.fill(
                                  child: CustomPaint(
                                      painter: CustomTimerPainter(
                                    animation: controller,
                                    backgroundColor: Colors.white,
                                    color: themeData.indicatorColor,
                                  )),
                                ),
                                Align(
                                  alignment: FractionalOffset.center,
                                  child: Column(
                                    mainAxisAlignment:
                                        MainAxisAlignment.spaceEvenly,
                                    crossAxisAlignment:
                                        CrossAxisAlignment.center,
                                    children: <Widget>[
                                      _editTitleTextField(),
                                    ],
                                  ),
                                ),
                              ],
                            ),
                          ),
                        ),
                      ),
                    ],
                  ),
                ),
              ],
            );
          }),
    );
  }
  void flutterNotification(){

  }
  Widget _editTitleTextField() {
    if (_isEditingText)
      return Center(
        child: TextField(
          style: TextStyle(color: Colors.white),
          cursorColor: Colors.white,
          keyboardType: TextInputType.number,
          inputFormatters: <TextInputFormatter>[ LengthLimitingTextInputFormatter(2),
            FilteringTextInputFormatter.allow(RegExp(r'[1-12]')),
          ],
          decoration: new InputDecoration(
              border: InputBorder.none,
              focusedBorder: InputBorder.none,
              enabledBorder: InputBorder.none,
              errorBorder: InputBorder.none,
              disabledBorder: InputBorder.none,
             // contentPadding:
              //EdgeInsets.only(left: 15, bottom: 11, top: 11, right: 15),
              hintText: "00:00"),
          onSubmitted: (String newValue) {
            setState(() {
              initialText = newValue;
              _getLimitUsage();
              _isEditingText = false;
            });
          },
          autofocus: true,
          controller: _editingController,
        ),
      );
    return InkWell(
      onTap: () {
        setState(() {
          _isEditingText = true;
        });
      },
      child: Text(
        initialText,
        style: TextStyle(
          color: Colors.white,
          fontSize: 60.0,
        ),
      ),
    );
  }
}
