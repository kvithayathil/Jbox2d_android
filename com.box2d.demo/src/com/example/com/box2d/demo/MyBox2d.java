package com.example.com.box2d.demo;

import org.jbox2d.collision.AABB;
import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.FixtureDef;
import org.jbox2d.dynamics.World;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class MyBox2d extends Activity {

	//Experimenting with rate and iterations
	private final static int RATE = 10;// 屏幕到现实世界的比例 10px：1m;
	private AABB worldAABB;// 创建 一个管理碰撞的世界
	private World world;
	private float timeStep = 1 / 60;// 模拟的的频率
	private int iterations = 3;// 迭代越大，模拟约精确，但性能越低
	//private int sleepTime = 10;
	private int positionIterations = 2;
	
	//Red box
	private Body body;
	//Ball 1
	private Body body2;
	//Ball 2
	private Body body3;
	//Container View
	private MyView myView;
	private Handler mHandler;
	
	float posX = 0;
	float posY = 0;

	private static final String TAG = MyBox2d.class.getSimpleName();
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		//Not used
		worldAABB = new AABB();
		worldAABB.lowerBound.set(-100.0f, 1000.0f);
		// 上下界，以屏幕的左上方为 原点，如果创建的刚体到达屏幕的边缘的话，会停止模拟
		worldAABB.upperBound.set(100.0f, 100.0f); // 注意这里使用的是现实世界的单位

		//Gravity properties
		Vec2 gravity = new Vec2(0.0f, -10.0f);
		boolean doSleep = true;

		world = new World(gravity);
		
		world.setAllowSleep(doSleep);// 创建世界
		
		//Android thread controller
		mHandler = new Handler();

		
		createBox(160, 470, 160, 10, true);
		createBox1(160, 150, 160, 10, false);

		createCircle(160, 100, 10);
		createCircle1(150, 60, 10);
		//timeStep = 1.0f / 60.0f;

		//Creating the view to be displayed
		myView = new MyView(this);
		
		//Set the view to the activity
		setContentView(myView);

	}
	

	
	

	//Stop update when app is minimized
	@Override
	protected void onPause() {
		super.onPause();
		mHandler.removeCallbacks(update);
	}

	@Override
	protected void onResume() {
		super.onResume();
		mHandler.post(update);
	}



	//New thread to execute the world step and update the views with the new coordinates
	private Runnable update = new Runnable() {
				
		public void run() {
			world.step(timeStep, iterations, positionIterations);// 开始模拟
			
			
			Vec2 position = body.getPosition();
			Vec2 position1 = body2.getPosition();
			Vec2 position2 = body3.getPosition();
			myView.x = position.x * RATE;
			myView.y = position.y * RATE;

			myView.x1 = position1.y * RATE;
			myView.y1 = position1.y * RATE;

			myView.x2 = position2.x * RATE;
			myView.y2 = position2.y  * RATE;
			
			
			Log.d(TAG, "Body1 x:" + position.x + " y: " + position.y);
			Log.d(TAG, "Body1 x:" + position1.x + " y: " + position1.y);
			Log.d(TAG, "Body2 x:" + position2.x + " y: " + position2.y);

			myView.update();

			
			mHandler.postDelayed(update, (long) timeStep * 1000);
		}
	};

	
	//Create box one (Red one)
	public void createBox(float x, float y, float half_width,
			float half_height, boolean isStatic) {
		PolygonShape shape = new PolygonShape();
		shape.setAsBox(half_width, half_height);

		FixtureDef fixDef = new FixtureDef();
		BodyDef bodyDef = new BodyDef();
		bodyDef.position.set(x, y);

		if (isStatic) {
			bodyDef.type = BodyType.KINEMATIC;
		} else {
			bodyDef.type = BodyType.DYNAMIC;
		}
		fixDef.density = 2.0f;
		fixDef.shape = shape;
		fixDef.friction = 0.8f;
		fixDef.restitution = 0.3f;

		Body body1 = world.createBody(bodyDef);
		body1.createFixture(fixDef);
	}

	public void createCircle(float x, float y, float radius) {
		CircleShape shape = new CircleShape();
		shape.setRadius(radius);

		FixtureDef fixedDef = new FixtureDef();
		fixedDef.density = 7;
		fixedDef.friction = 0.2f;
		fixedDef.shape = shape;

		BodyDef bodyDef = new BodyDef();
		bodyDef.position.set(x / RATE, y / RATE);
		bodyDef.type = BodyType.DYNAMIC;

		
		body2 = world.createBody(bodyDef);
		body2.createFixture(fixedDef);
	}

	public void createCircle1(float x, float y, float radius) {
		CircleShape shape = new CircleShape();
		shape.setRadius(radius);
		
		FixtureDef fixedDef = new FixtureDef();
		fixedDef.density = 7;
		fixedDef.friction = 0.2f;
		
		fixedDef.shape = shape;
		

		BodyDef bodyDef = new BodyDef();
		bodyDef.position.set(x / RATE, y / RATE);
		bodyDef.type = BodyType.DYNAMIC;

		body3 = world.createBody(bodyDef);
		body3.createFixture(fixedDef);
	}

	//Red box
	public void createBox1(float x, float y, float half_width,
			float half_height, boolean isStatic) {
		PolygonShape shape = new PolygonShape();
		shape.setAsBox(half_width / RATE, half_height / RATE);

		FixtureDef fixedDef = new FixtureDef();


		BodyDef bodyDef = new BodyDef();
		bodyDef.position.set(x / RATE, y / RATE);
		
		//Check if ground
		if (isStatic) {
			bodyDef.type = BodyType.STATIC;
		} else {
			bodyDef.type = BodyType.DYNAMIC;
		}
		fixedDef.density = 2f;
		fixedDef.friction = 0.3f;
		
		fixedDef.shape = shape;


		body = world.createBody(bodyDef);
		body.createFixture(fixedDef);

	}
	
	/**
	 * Getting the screen width/height in pixels
	 * @return Point object with max width/height
	 */
	private Point getScreenDimens() {
		
		Point point = new Point();
		
		Display display = getWindowManager().getDefaultDisplay();
		display.getSize(point);
	
		return point;
	}

	/**
	 * Custom view that contains the various elements of the view together (i.e. balls, ground, boxes)
	 * @author KV_87
	 *
	 */
	class MyView extends View {
		Context context;
		Canvas canvas;
		Point screenDimens;
		public float x = 160, y = 150;
		public float x1 = 160, y1 = 100;
		public float x2 = 150, y2 = 60;

		public MyView(Context context) {
			super(context);
			
			this.context = context;
			
			screenDimens = getScreenDimens();
		}

		//Red box
		public void drawBox(float x, float y) {
			Paint mPaint = new Paint();
			mPaint.setAntiAlias(true);
			mPaint.setColor(Color.RED);
			canvas.drawRect(x - 160, y - 10, x + 160, y + 10, mPaint);
		}

		//Draw blue ground
		public void drawGround() {
			Paint mPaint = new Paint();
			mPaint.setAntiAlias(true);
			mPaint.setColor(Color.BLUE);
						
			canvas.drawRect(0, screenDimens.y - 200, screenDimens.x, screenDimens.y, mPaint);
		}

		//Green ball
		public void drawCircle(float x1, float y1) {
			Paint mPaint = new Paint();
			mPaint.setAntiAlias(true);
			mPaint.setColor(Color.GREEN);
			canvas.drawCircle(x1, y1, 10, mPaint);
		}

		//Force a redraw
		public void update() {
			postInvalidate();
		}

		protected void onDraw(Canvas canvas) {
			super.onDraw(canvas);
			this.canvas = canvas;
			drawGround();
			drawBox(x, y);
			drawCircle(x1, y1);
			drawCircle(x2, y2);
		}
		
		/**
		 * Getting the screen width/height in pixels
		 * @return Point object with max width/height
		 */
		private Point getScreenDimens() {
			
			Point point = new Point();
			
			WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
			Display display = windowManager.getDefaultDisplay();
		
			//Get the dimensions of the screen and set it to the point
			display.getSize(point);
			
			return point;
		}

	}
}