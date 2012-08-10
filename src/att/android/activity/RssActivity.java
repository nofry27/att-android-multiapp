package att.android.activity;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.PopupWindow;
import att.android.adapter.RssAdapter;
import att.android.bean.News;
import att.android.network.ReadRssNetwork;
import att.android.util.ParseXMLRss;

import com.example.multiapp.R;
import com.quickaction.popup.ActionItem;
import com.quickaction.popup.QuickAction;
import com.quickaction.popup.QuickAction.OnActionItemClickListener;

public class RssActivity extends Activity implements OnItemClickListener,
		OnClickListener {
	private ListView mListView;
	private RssAdapter mNewsAdapter;
	private ArrayList<News> mNews;
	private String strUrl;
	private WebView mWebView;
	private Button btnBack;
	private Button btnWebName;
	private QuickAction mQuickAction;
	private OnDataNetwork mData;
	private Thread thread;
	private boolean didInit = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_read_rss);
		if (didInit == false) {
			didInit = true;
			initVariables(this);
		}
		initViews();
		initActions();

	}

	public void onItemClick(AdapterView<?> arg0, View arg1, int position,
			long arg3) {
		mNewsAdapter.getItem(position);
		News item = mNewsAdapter.getItem(position);
		String strUrl = item.getmUrl();
		mWebView.setVisibility(View.VISIBLE);
		btnBack.setVisibility(View.VISIBLE);
		mWebView.getSettings().setJavaScriptEnabled(true);
		mWebView.setWebViewClient(new WebViewClient() {
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				view.loadUrl(url);
				return true;
			}
		});
		mWebView.loadUrl(strUrl);
	}

	public void onNothingSelected(AdapterView<?> arg0) {

	}

	public void initVariables(Context context) {
		mNews = new ArrayList<News>();
		mNewsAdapter = new RssAdapter(this, R.id.titleNews, mNews);
		strUrl = "http://www.tinhte.vn/rss/";
		mQuickAction = new QuickAction(this);
		mData = new OnDataNetwork();
	}

	public void initViews() {
		mListView = (ListView) this.findViewById(R.id.listNews);
		mWebView = (WebView) this.findViewById(R.id.webView);
		btnBack = (Button) this.findViewById(R.id.btn_webBack);
		btnWebName = (Button) this.findViewById(R.id.web_name);
	}

	public void initActions() {
		mNewsAdapter.notifyDataSetChanged();
		mListView.setOnItemClickListener(this);
		mListView.setAdapter(mNewsAdapter);

		mWebView.setVisibility(View.GONE);
		btnBack.setOnClickListener(this);
		btnBack.setVisibility(View.INVISIBLE);
		btnWebName.setOnClickListener(this);
		ActionItem actionTinhTe = new ActionItem();
		actionTinhTe.setTitle("Tinhte.vn");
		ActionItem actionVnEx = new ActionItem();
		actionVnEx.setTitle("VnExpress.net");
		ActionItem actionGame = new ActionItem();
		actionGame.setTitle("Gamethu.net");
		ActionItem action24h = new ActionItem();
		action24h.setTitle("24h.com.vn");
		mQuickAction.addActionItem(actionTinhTe);
		mQuickAction.addActionItem(actionVnEx);
		mQuickAction.addActionItem(actionGame);
		mQuickAction.addActionItem(action24h);
		mQuickAction
				.setOnActionItemClickListener(new OnActionItemClickListener() {

					public void onItemClick(QuickAction source, int pos,
							int actionId) {
						switch (pos) {
						case 0:
							strUrl = "http://www.tinhte.vn/rss/";
							break;
						case 1:
							strUrl = "http://vnexpress.net/rss/gl/trang-chu.rss";
							break;
						case 2:
							strUrl = "http://gamethu.vnexpress.net/rss/gt/diem-tin.rss";
							break;
						case 3:
							strUrl = "http://www.24h.com.vn/upload/rss/tintuctrongngay.rss";
							break;
						}
						Log.i("popup-----", pos + "-------" + actionId);
						btnWebName
								.setText(source.getActionItem(pos).getTitle());
						mNewsAdapter.clear();
						mData.cancel(true);
						mData = new OnDataNetwork();
						mData.execute(strUrl);
					}
				});
		mData.execute(strUrl);
	}

	public void onClick(View v) {
		if (v == btnBack) {
			btnBack.setVisibility(View.INVISIBLE);
			mWebView.setVisibility(View.GONE);
		}
		if (v == btnWebName) {
			mQuickAction.show(v);
		}
	}

	private class OnDataNetwork extends AsyncTask<String, Integer, Integer> {

		@Override
		protected Integer doInBackground(String... url) {
			URL mUrl;
			try {
				mUrl = new URL(url[0]);
				URLConnection ucon = mUrl.openConnection();
				ucon.connect();
				InputStream mIs = mUrl.openStream();
				mNews = ParseXMLRss.getDataFromXML(mIs);
				publishProgress();
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			super.onProgressUpdate(values);
			for (News itm : mNews) {
				mNewsAdapter.add(itm);
			}
			mNewsAdapter.notifyDataSetChanged();
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
		}
	}
}
