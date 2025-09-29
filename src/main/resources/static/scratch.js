db.lead.aggregate([
    {
        $match: {
            _id: ObjectId("68c0990b0e12095399b125d1")
        }
    },
    {
        $lookup: {
            from: "users",
            let: {cid: "$assignedToId"},
            pipeline: [
                {$match: {$expr: {$eq: ["$_id", "$$cid"]}}},
                {$project: {username: 1, _id: 0}}
            ],
            as: "user"
        }
    }
])