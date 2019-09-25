# Intro
Enhanced DAO access components including DAO/Cache/Partition support.

This module contains two main purposes:

* Generic and simple JDBC data access support(Enhanced).
* Simple and optional two layer cache support.
* Simple definition and usage of redis service

# enhanced-data-cache
## Redis Support
### Annotations
* EnableRedisTemplate
* EnableRedisService

### Features
* Redis Support(XML/Annotation/Manually)
* Distributed Lock(Based on Redis)
* Distributed Flow Control(For Quota)

## Cache Support
### Annotations
* InitCache
* EnableCache
* EnableCacheConfig
* EnableCacheEvict

### Features
* Two Layer Caching(Remote/Local)
* Evicting Support
* ELExpression Support in Customized Key

See the [documentation](enhanced-data-cache/README.md).

# enhanced-data-dao-api
API package for DAO support.

It's not recommended to use enhanced model in interface but if you do so, please add this to the dependencies list for your interface package. Everything will be ok.

See the [documentation](enhanced-data-dao-api/README.md).

eg. DynamicQueryFilter  

# enhanced-data-dao
Implementation of DAO components under mybatis.

See the [documentation](enhanced-data-dao/README.md).

# enhanced-data-dao-sharding
Implementation of sharding DAO.

See the [documentation](enhanced-data-dao-sharding/README.md).

# Change log
## 1.1.7
* Fix bug in implementation of `DistributedLock.keep`

## 1.1.6
* Make sure `EnhancedDaoScanner` initialized before `EnhancedDao`s initialized

## 1.1.5
* Add `mapperLocations` to `EnableEnhancedDao` to support parsing custom mappers

## 1.1.4
* Fix local cache ttl bug

## 1.1.3
* Fix `InitCache` initializing bug

## 1.1.2
* Fix malformed assembling when using multiple `order by`

## 1.1.1
* Upgrade enhanced-parent to 1.0.3

## 1.1.0
Feature:
* Documents work
* Add operation for unsorted set in RedisService
* Add generic interface into RedisService

Fix:
* Expiration logic in DistributedLock
* Add unit test for interface RedisService

## 1.0.1
* Restructure and redefine `RedisService`
* Make `DistributedLock` up to date

## 1.0.0
* Initial for open source

