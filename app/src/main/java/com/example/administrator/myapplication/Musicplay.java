package com.example.administrator.myapplication;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.SyncStateContract;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import app.minimize.com.seek_bar_compat.SeekBarCompat;

/**
 * Created by Administrator on 2016-05-27.
 */
public class Musicplay extends Fragment{
    MainActivity activity;
    View view;
    MediaPlayer music;
    SeekBarCompat seekbar;
    ImageView mimage;
    int current_music_pos= 0;
    List mList;
    Button stopPlayBtn, backMusicBtn, frontMusicBtn;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
// Inflate the layout for this fragment
        view = inflater.inflate(R.layout.musicplay, container, false);
        activity = ((MainActivity)getActivity());
        stopPlayBtn = (Button) view.findViewById(R.id.button1);
        backMusicBtn = (Button) view.findViewById(R.id.button2);
        frontMusicBtn = (Button) view.findViewById(R.id.button3);
        seekbar = (SeekBarCompat) view.findViewById(R.id.seekBar1);
        seekbar.setThumbColor(Color.RED);
        seekbar.setProgressColor(Color.WHITE);
        seekbar.setProgressBackgroundColor(Color.GRAY);
        seekbar.setThumbAlpha(255);
        mimage = (ImageView) view.findViewById(R.id.Mimage);


        stopPlayBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (music.isPlaying()) {
                    music.pause();
                    try {
                        music.prepare();
                    } catch (IllegalStateException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    music.getCurrentPosition();

                    stopPlayBtn.setBackgroundResource(R.drawable.play);
                    seekbar.setProgress(music.getCurrentPosition());
                } else {
// 재생중이 아니면 실행될 작업 (재생)

                    music.start();
                    mimage.setVisibility(View.VISIBLE);
                    stopPlayBtn.setBackgroundResource(R.drawable.stop);

                    Thread();
                }

                activity.startDataSendService(Constants.SEND_STATE);

            }
        });

        backMusicBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (current_music_pos == 0) {
                    current_music_pos = mList.size();
                }
                music.stop();
                current_music_pos = current_music_pos - 1;
                music = MediaPlayer.create(getContext(), Uri.parse((String) mList.get(current_music_pos)));
                File file = new File((String) mList.get(current_music_pos));
                mimage.setImageBitmap(getAlbumArt(getContext(), file));
                seekbar.setMax(music.getDuration());
                music.start();
                stopPlayBtn.setBackgroundResource(R.drawable.stop);
                setFilename((String) mList.get(current_music_pos));
                Thread();
                activity.sendToMusic = file;
                activity.startDataSendService(Constants.SEND_MUSIC);
            }
        });
        frontMusicBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (current_music_pos == mList.size() - 1) {
                    current_music_pos = -1;
                }
                music.stop();
                current_music_pos = current_music_pos + 1;
                music = MediaPlayer.create(getContext(), Uri.parse((String) mList.get(current_music_pos)));
                File file = new File((String) mList.get(current_music_pos));
                mimage.setImageBitmap(getAlbumArt(getContext(), file));
                seekbar.setMax(music.getDuration());
                music.start();
                stopPlayBtn.setBackgroundResource(R.drawable.stop);
                setFilename((String) mList.get(current_music_pos));
                Thread();
                activity.sendToMusic = file;
                activity.startDataSendService(Constants.SEND_MUSIC);
            }
        });
        seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                activity.startDataSendService(Constants.SEND_POSITION);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,
                                          boolean fromUser) {
                // TODO Auto-generated method stub
                if (fromUser)
                    music.seekTo(progress);


            }
        });

        return view;
    }

    public void setFilename(String file_name) {
        TextView tx = (TextView) view.findViewById(R.id.tvPath);
        //String path = file_name;
        String fileName = new File(file_name).getName();
        tx.setText(fileName);
    }

    public void Thread() {
        Runnable task = new Runnable() {
            public void run() {
                /**
                 * while문을 돌려서 음악이 실행중일때 게속 돌아가게
                 */
                while (music.isPlaying()) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    /**
                     * music.getCurrentPosition()은 현재 음악 재생 위치를 가져오는 구문
                     */
                    seekbar.setProgress(music.getCurrentPosition());

                    music.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

                        public void onCompletion(MediaPlayer mp) {
                            if (current_music_pos == mList.size() - 1) {
                                current_music_pos = -1;
                            }
                            current_music_pos = current_music_pos + 1;
                            music = MediaPlayer.create(getContext(), Uri.parse((String) mList.get(current_music_pos)));
                            File file = new File((String) mList.get(current_music_pos));
                            mimage.setImageBitmap(getAlbumArt(getContext(), file));
                            seekbar.setMax(music.getDuration());
                            music.start();
                            setFilename((String) mList.get(current_music_pos));
                            Thread();
                        }
                    });
                }
            }
        };
        Thread thread = new Thread(task);
        thread.start();
    }


    public void setMusic(String file_path,List mList,int current_music_pos){
        File file = new File(file_path);
        setFilename(file_path);
        this.mList = mList;
        this.current_music_pos = current_music_pos;
        mimage.setImageBitmap(getAlbumArt(getContext(), file));
        if (music != null) {
            music.stop();
            stopPlayBtn.setBackgroundResource(R.drawable.play);
        }
        music = MediaPlayer.create(getContext(), Uri.parse(file_path));
        music.setLooping(false);
        stopPlayBtn.setBackgroundResource(R.drawable.stop);
        seekbar.setMax(music.getDuration());
        music.start();
        Thread();
    }

    public void receiveMusicStart() {
        if(music != null) {
            music.stop();
            music = null;
        }
        music = MediaPlayer.create(getContext(), Uri.parse("/storage/emulated/0/Download/a.mp3"));
        music.start();
    }


    private Bitmap getAlbumArt(Context context, File mp3File) {
        Uri ArtworkUri = Uri.parse("content://media/external/audio/albumart");
        long albumId = 0;
        String mediaPath = mp3File.getAbsolutePath();
        String projection[] = {MediaStore.Audio.Media.ALBUM_ID};
        String selection = MediaStore.Audio.Media.DATA + " LIKE ? ";
        String selectionArgs[] = {mediaPath};
        Cursor cursor = context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection, selection, selectionArgs, null);
        if (cursor != null) {
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                albumId = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));
            }
            cursor.close();
        }
        if (albumId > 0) {
            Uri albumArtUri = ContentUris.withAppendedId(ArtworkUri, albumId);
            ContentResolver res = context.getContentResolver();
            Bitmap bitmap = null;
            try {
                InputStream input = res.openInputStream(albumArtUri);
                bitmap = BitmapFactory.decodeStream(res.openInputStream(albumArtUri));
                input.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return bitmap;
        }
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.start);
        return bitmap;
    }

}