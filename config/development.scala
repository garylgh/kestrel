import com.twitter.conversions.storage._
import com.twitter.conversions.time._
import com.twitter.logging.config._
import com.twitter.ostrich.admin.config._
import net.lag.kestrel.config._

new KestrelConfig {
  listenAddress = "0.0.0.0"
  memcacheListenPort = 22133
  textListenPort = 2222
  thriftListenPort = 2229

  queuePath = System.getenv("HOME") + "/queues"

  clientTimeout = 30.seconds

  expirationTimerFrequency = 1.second

  maxOpenTransactions = 100

  // default queue settings:
  default.journalSize = 16.megabytes
  default.defaultReader.maxMemorySize = 128.megabytes

  admin.httpPort = 2223

  admin.statsNodes = new StatsConfig {
    reporters = new TimeSeriesCollectorConfig
  }

  queues = new QueueBuilder {
    // keep items for no longer than a half hour, and don't accept any more if
    // the queue reaches 1.5M items.
    name = "weather_updates"
    defaultReader.maxAge = 1800.seconds
    defaultReader.maxItems = 1500000
  } :: new QueueBuilder {
    // don't keep a journal file for this queue. when kestrel exits, any
    // remaining contents will be lost.
    name = "transient_events"
    journaled = false
  } :: new QueueBuilder {
    name = "jobs_pending"
    defaultReader.expireToQueue = "jobs_ready"
    defaultReader.maxAge = 30.seconds
  } :: new QueueBuilder {
    name = "jobs_ready"
    syncJournal = 0.seconds
    defaultReader.puntErrorToQueue = "jobs_pending"
    defaultReader.puntManyErrorsCount = 10
    defaultReader.puntManyErrorsToQueue = "spam"
  } :: new QueueBuilder {
    name = "spam"
  } :: new QueueBuilder {
    name = "spam0"
  } :: new QueueBuilder {
    name = "small"
    journalSize = 1.megabyte
    defaultReader.maxSize = 128.megabytes
    defaultReader.maxMemorySize = 16.megabytes
    defaultReader.discardOldWhenFull = true
  } :: new QueueBuilder {
    name = "slow"
    syncJournal = 10.milliseconds
  }

  debugLogQueues = List()

  loggers = new LoggerConfig {
    level = Level.INFO
    handlers = new FileHandlerConfig {
      filename = "/var/log/kestrel/kestrel.log"
      roll = Policy.Never
    }
  }
}

