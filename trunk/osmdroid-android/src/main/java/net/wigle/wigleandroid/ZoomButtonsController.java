package net.wigle.wigleandroid;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.view.MotionEvent;
import android.view.View;

/**
 * android 1.5 compatible zoom controller, works like android.widget.ZoomButtonsController in 1.5 and up, 
 * the class exists on 1.5 devices you just can't compile with it against a 1.5 sdk. fun.
 * 
 * Public Domain license, feel free to use wherever. I'd appreciate credit given.
 * @author bobzilla at wigle.net
 *
 */
public class ZoomButtonsController {

  private static final Logger logger = LoggerFactory.getLogger(ZoomButtonsController.class);

  private static Class ZOOM_CLASS;
  private static Class LISTENER_CLASS;
  private static Method setOnZoomListener;
  private static Method setVisible;
  private static Method setZoomInEnabled;
  private static Method setZoomOutEnabled;
  private static Method onTouch;
  private static Method isVisible;
  
  private Object controller;
  
  static {
    try {
      ZOOM_CLASS =     Class.forName("android.widget.ZoomButtonsController");
      for ( Class clazz : ZOOM_CLASS.getDeclaredClasses() ) {
        // info( "clazz: " + clazz.getSimpleName() );
        if ( "OnZoomListener".equals( clazz.getSimpleName() ) ) {
          // info( "listener_class set" );
          LISTENER_CLASS = clazz;
        }
      }
      setOnZoomListener = ZOOM_CLASS.getMethod("setOnZoomListener", LISTENER_CLASS );
      setVisible = ZOOM_CLASS.getMethod("setVisible", Boolean.TYPE );
      setZoomInEnabled = ZOOM_CLASS.getMethod("setZoomInEnabled", Boolean.TYPE );
      setZoomOutEnabled = ZOOM_CLASS.getMethod("setZoomOutEnabled", Boolean.TYPE );
      onTouch = ZOOM_CLASS.getMethod("onTouch", View.class, MotionEvent.class);
      isVisible = ZOOM_CLASS.getMethod("isVisible" );
    }
    catch ( Exception ex ) {
      // don't have it
      logger.info( "no zoom buttons: " + ex );
    }
  }
  
  public interface OnZoomListener {
    public void onZoom(boolean zoomIn);
    public void onVisibilityChanged(boolean visible);
  }
  
  public ZoomButtonsController ( final View view ) {
    if ( ZOOM_CLASS != null ) {
      try {
        controller = ZOOM_CLASS.getConstructor( View.class ).newInstance( view );
      }
      catch ( Exception ex ) {
        logger.error( "exception instantiating: " + ex );
      }
    }
  }
  
  public void setOnZoomListener( final OnZoomListener listener ) {
    if ( controller != null ) {
      try {
        final InvocationHandler handler = new InvocationHandler() {
          public Object invoke( Object object, Method method, Object[] args ) {
            logger.info( "invoke: " + method.getName() + " listener: " + listener );
            if ( "onZoom".equals( method.getName() ) ) {
              listener.onZoom( (Boolean) args[0] );
            }
            else if ( "onVisibilityChanged".equals( method.getName() ) ) {
              listener.onVisibilityChanged( (Boolean) args[0] );
            }
            else {
              logger.info( "unhandled listener method: " + method );
            }
            return null;
          }
        };
        Object proxy = Proxy.newProxyInstance( LISTENER_CLASS.getClassLoader(), 
            new Class[]{ LISTENER_CLASS }, handler );
        setOnZoomListener.invoke( controller, proxy );
      }
      catch ( Exception ex ) {
        logger.error( "setOnZoomListener exception: " + ex );
      }
    }
  }
  
  public void setVisible( final boolean visible ) {
    if ( controller != null ) {
      try {
        setVisible.invoke( controller, visible );
      }
      catch ( Exception ex ) {
        logger.error( "setVisible exception: " + ex );
      }
    }
  }
  
  public void setZoomInEnabled( final boolean enabled ) {
    if ( controller != null ) {
      try {
        setZoomInEnabled.invoke( controller, enabled );
      }
      catch ( Exception ex ) {
        logger.error( "setZoomInEnabled exception: " + ex );
      }
    }
  }
  
  public void setZoomOutEnabled( final boolean enabled ) {
    if ( controller != null ) {
      try {
        setZoomOutEnabled.invoke( controller, enabled );
      }
      catch ( Exception ex ) {
        logger.error( "setZoomOutEnabled exception: " + ex );
      }
    } 
  }
  
  public boolean onTouch(final View v, final MotionEvent m ) {
    if ( controller != null ) {
      try {
    	return (Boolean) onTouch.invoke(controller, v, m);
      }
      catch ( Exception ex ) {
        logger.error( "onTouch exception: " + ex );
      }
    } 
    return false;
  }
  public boolean isVisible() {
    if ( controller != null ) {
      try {
    	return (Boolean) isVisible.invoke(controller);
      }
      catch ( Exception ex ) {
        logger.error( "isVisible exception: " + ex );
      }
    } 
    return false;
  }
}
