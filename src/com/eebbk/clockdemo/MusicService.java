package com.eebbk.clockdemo;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.eebbk.utils.ClockUtils;
import com.eebbk.utils.DatabaseManager;

public class MusicService extends Service {

	private MediaPlayer mMediaPlayer = null;
	private AlarmManager mAlarmManager = null;

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onStart(Intent intent, int startId) {
		// TODO Auto-generated method stub
		super.onStart(intent, startId);
	}


	@Override
	public int onStartCommand(final Intent intent, int flags, int startId) {

		new Thread(new Runnable() {

			@Override
			public void run() {

				mMediaPlayer = ClockUtils.getMediaPlayer();
				mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);


				int i = intent.getIntExtra("intent", 0);
				switch (i) {
				//�㲥����������ͼ����ʼ����
				case 1:
					playMusic();
					Log.i("aaa", "�����ӣ��������е�������");
					break;
					//ֹͣ����
				case 2:
					mMediaPlayer.pause();
					String rate = intent.getStringExtra("rate");
					String id = intent.getStringExtra("id");
					if("��һ��".equals(rate)){
						ContentValues cv = new ContentValues();
						cv.put(AlarmDatabase.ALARM_BUTTON_FLAG, 0);
						DatabaseManager.updateDB(AlarmDatabase.TABLE_NAME, cv, AlarmDatabase.ALARM_ID+"=?", new String[]{id});

						Toast.makeText(getApplicationContext(), "�Ѿ����������ݿ��ﰴť��״̬", Toast.LENGTH_SHORT).show();
					}
					break;
					//��˯һ�����Ȼ���ٴ�����
				case 3:
					Long lazy = intent.getLongExtra("lazy", 5*60*1000l);
					mMediaPlayer.pause();
					mMediaPlayer.stop();
					mAlarmManager = (AlarmManager) getApplicationContext().getSystemService(ALARM_SERVICE);
					Intent ia = new Intent(getApplicationContext(), MyBroadcastReceiver.class);
					PendingIntent pi = PendingIntent.getBroadcast(getApplicationContext(), 0, ia, 0);
					mAlarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis()+lazy, pi);
					break;
				default:
					break;
				}
				//��������
				stopSelf();

			}
		}).start();

		return super.onStartCommand(intent, flags, startId);
	}

	//������������
	private void playMusic() {

		try {
			AssetFileDescriptor fileDescriptor = getApplicationContext().getAssets().openFd("coldwind.mp3");
			mMediaPlayer.setDataSource(fileDescriptor.getFileDescriptor(),fileDescriptor.getStartOffset(), fileDescriptor.getLength());
			mMediaPlayer.prepare();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		mMediaPlayer.seekTo(0);
		mMediaPlayer.start();

		Log.i("aaa", "�����ܵ����������");
		//mMediaPlayer.start();
	}

}
