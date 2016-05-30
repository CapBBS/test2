package com.example.administrator.myapplication;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016-05-27.
 */
public class Musiclist extends Fragment {

    private List mFileList = new ArrayList();
    private List mList = new ArrayList();    //   위와 같음.
    private File Musicfolder1 = new File(Environment.getExternalStorageDirectory() + "/Music", "");  // 뮤직폴더에서 찾기위해
    private File Musicfolder2 = new File(Environment.getExternalStorageDirectory() + "/Download", ""); // 다운로드폴더에서 찾기위해
    private static final String[] FTYPE = {"mp3", "wav"}; // 찾는타입 (.mp3 , .wav)형식 찾음
    private static String file_path = null;  //음악파일의 uri를 string으로 받음
    int Marg, Lleng = 0;
    MediaPlayer music;


        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
// Inflate the layout for this fragment
            final MainActivity activity = ((MainActivity)getActivity());
            View view = inflater.inflate(R.layout.musiclist, container, false);
            ListView lvFileControl = (ListView) view.findViewById(R.id.lvFileControl);

            mFileList.clear();
            loadAllAudioList(Musicfolder1);// music
            loadAllAudioList(Musicfolder2);// download

            ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), R.layout.musiclist_item, mFileList);
            lvFileControl.setAdapter(adapter);
            lvFileControl.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                    int currentMusicPos = arg2;
                    file_path = (String) mList.get(arg2);
                    activity.set_media_state(file_path, mList, currentMusicPos);
                    activity.sendToMusic = new File(file_path);
                    activity.startDataSendService(Constants.SEND_MUSIC);
                    activity.tabLayout.getTabAt(0).select();
                }
            });

            return view;
        }

    private void loadAllAudioList(File file) {
        if (file != null && file.isDirectory()) {
            File[] children = file.listFiles();
            if (children != null) {

                for (int i = 0; i < children.length; i++) {
                    if (children[i] != null) {
                        for (int j = 0; j < FTYPE.length; j++) {
                            if (FTYPE[j].equals(children[i].getName().substring(children[i].getName().lastIndexOf(".") + 1,
                                    children[i].getName().length()))) {
                                mFileList.add(children[i].getName());
                                mList.add(children[i].getAbsolutePath());

                            }
                        }
                    }
                    loadAllAudioList(children[i]);
                }
            }
        }
    }




}
