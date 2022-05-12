Feature: Business Object Audit Events

  Scenario: Successfully creating a single event
    Given a business object with type sample and a random id
    And a collection of activity events
      | timestamp                     | summary               | type    | data           |
      | 2021-12-18T00:00:00.000+00:00 | sample object created | created | {"foo": "bar"} |
    When the events for the business object are published
    Then the api response should have status code 201

  Scenario Template: Attempting to create event with invalid request
    Given a business object with type invalid and a random id
    And the invalid request body defined at <resourcePath>
    When a request is made to create the event
    Then the resulting status code should be 400

    Examples:
      | resourcePath                                                                        |
      | requests/invalid-post-events/activity-event-with-no-data.json                       |
      | requests/invalid-post-events/no-event-data.json                                     |
      | requests/invalid-post-events/no-request-context.json                                |
      | requests/invalid-post-events/no-timestamp.json                                      |
      | requests/invalid-post-events/not-json.txt                                           |
      | requests/invalid-post-events/state-change-event-with-neither-new-nor-old-state.json |

  Scenario Template: Creating and retrieving events
    Given a business object with type sample and a random id
    And a collection of activity events
      | timestamp                     | summary                               | type           | data            |
      | 2021-12-18T00:00:00.530+00:00 | sample object created                 | created        | {"foo": "bar"}  |
      | 2021-12-18T00:00:04.000+00:00 | sample object marked for deletion     | deleted        |                 |
      | 2021-12-18T00:00:02.000+00:00 | user requests access to sample object | access_request | {"user": "123"} |
    And a collection of state change events
      | timestamp                     | summary                    | new_state      | old_state      |
      | 2021-12-18T00:00:01.400+00:00 | sample object data updated | {"foo": "baz"} | {"foo": "bar"} |
      | 2021-12-18T00:00:05.010+00:00 | sample object deleted      |                | {"foo": "baz"} |
    When the events for the business object are published
    Then getting business object audit events should return as many events as were published
      | param     | value       |
      | sortOrder | <sortOrder> |
      # optional query parameters to be used in searches
      #| startTime |  |
      #| endTime |  |
      #| pageNumber |  |
      #| pageSize   |  |
    And the api response should have status code 200
    And events should be on a single page
    And events should be ordered by timestamp
    And events should contain all of the data defined in the requests

    Examples:
      | sortOrder |
      |           |
      | ASC       |
      | DESC      |

  Scenario: Creating and retrieving events on multiple pages
    Given a business object with type sample and a random id
    And a collection of activity events
      | timestamp                     | summary                               | type           | data            |
      | 2021-12-18T00:00:00.530+00:00 | sample object created                 | created        | {"foo": "bar"}  |
      | 2021-12-18T00:00:04.000+00:00 | sample object marked for deletion     | deleted        |                 |
      | 2021-12-18T00:00:02.000+00:00 | user requests access to sample object | access_request | {"user": "123"} |
    And a collection of state change events
      | timestamp                     | summary                    | new_state       | old_state       |
      | 2021-12-18T00:00:01.400+00:00 | sample object data updated | {"foo": "baz"}  | {"foo": "bar"}  |
      | 2021-12-18T00:00:01.400+00:00 | sample object data updated | {"foo": "baz"}  | {"foo": "bar"}  |
      | 2021-12-18T00:00:01.401+00:00 | sample object data updated | {"foo": "baz1"} | {"foo": "baz"}  |
      | 2021-12-18T00:00:01.402+00:00 | sample object data updated | {"foo": "baz2"} | {"foo": "baz1"} |
      | 2021-12-18T00:00:01.403+00:00 | sample object data updated | {"foo": "baz3"} | {"foo": "baz2"} |
      | 2021-12-18T00:00:01.404+00:00 | sample object data updated | {"foo": "baz4"} | {"foo": "baz3"} |
      | 2021-12-18T00:00:01.405+00:00 | sample object data updated | {"foo": "baz5"} | {"foo": "baz4"} |
      | 2021-12-18T00:00:01.406+00:00 | sample object data updated | {"foo": "baz6"} | {"foo": "baz5"} |
      | 2021-12-18T00:00:01.407+00:00 | sample object data updated | {"foo": "baz7"} | {"foo": "baz6"} |
      | 2021-12-18T00:00:05.010+00:00 | sample object deleted      |                 | {"foo": "baz"}  |
    When the events for the business object are published
    Then getting business object audit events should return as many events as were published
      | param    | value |
      | pageSize | 10    |
    And the api response should have status code 200
    And result should have a next page

  Scenario: Getting events for a nonexistent business object
    Given a business object with type doesnotexist and a random id
    Then getting business object audit events should return 0 events
      | param | value |
    And the api response should have status code 200
    And result should not have a next page

  Scenario Template: Getting events with invalid query parameters
    Given a business object with type sample and a random id
    Then getting business object audit events should fail with status 400
      | param      | value        |
      | sortOrder  | <sortOrder>  |
      | startTime  | <startTime>  |
      | endTime    | <endTime>    |
      | pageNumber | <pageNumber> |
      | pageSize   | <pageSize>   |

    Examples:
      | sortOrder | startTime                     | endTime                       | pageNumber | pageSize | invalidReason          |
      |           | 2021-12-18T00:00:05.010+00:00 | 2021-12-17T00:00:05.010+00:00 |            |          | negative time range    |
      | FOO       |                               |                               |            |          | nonexistent sort order |
      |           |                               |                               | -2         |          | negative page number   |
      |           |                               |                               |            | 3000000  | page size too large    |
